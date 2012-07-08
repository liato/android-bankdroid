import datetime
import os
import random
import subprocess
import urllib2

from PIL import Image

confirmed = {}
imgdata = {}

def guess_number(img):
    width, height = img.size
    img = img.load()
    for number in confirmed.values():
        matches = 0
        for point in number.points:
            if point[0] >= width:
                break
            color = img[point[0], point[1]]
            color = (color[0] << 16) + (color[1] << 8) + color[2]
            if (color == 0xffffff and not point[2]) or (color != 0xffffff and point[2]):
                matches += 1
        #print "matches: %d " % matches
        if matches == len(number.points):
            return number.number
    return None

def getCaptcha():
    print "Downloading new captcha..."
    r = urllib2.urlopen("https://mobil.nordea.se/banking-nordea/nordea-c3/captcha.png")
    f = open("captcha.png", "wb")
    f.write(r.read())
    f.close()
    img = Image.open("captcha.png")
    print "Captcha downloaded."
    return img

def extract_numbers(img):
    pixels = img.load()
    width, height = img.size
    numbers = []

    numberpart = False
    current_start = None
    current_end = None
    for x in range(width-1):
        numberpartcol = False
        for y in range(height-1):
            color = pixels[x, y]
            color = (color[0] << 16) + (color[1] << 8) + color[2]
            if color != 0xffffff:
                if not numberpart:
                    #print "Start at %d" % x
                    current_start = x
                numberpart = numberpartcol = True
                break

        if numberpart and not numberpartcol:            
            numberpart = False
            #print "End at %d" % (x-1,)
            current_end = x-1
            numbers.append((current_start, current_end))

    if current_end is None:
        numbers.append((current_start, width-1))

    return numbers

def numbers2text(img, numbers):
    text = []
    width, height = img.size
    for i, slice in enumerate(numbers):
        number = img.crop((slice[0], 0, slice[1], height-1))
        numguess = guess_number(number)
        text.append(numguess)
    return "".join(text)



class CaptchaNumber(object):
    def __init__(self, number, points):
        self.number = number
        self.points = points

    def __repr__(self):
        return '<CaptchaNumber(%d)>' % number
        

def main():
    while True:
        img = getCaptcha()
        width, height = img.size
        print "Image size: %dx%d" % (width, height)
        numbers = extract_numbers(img)
        print "Found %d numbers in captcha" % len(numbers)
        for i, slice in enumerate(numbers):
            number = img.crop((slice[0], 0, slice[1], height-1))

            fname = "tempslice.png"
            number.save(fname)
            #print "Number: width: %d, height: %d" % number.size
            pixels = number.load()
            numguess = guess_number(number)
            #os.system(fname)
            #if numguess is not None:
            #    correct = raw_input("Does the image display number %s? y/n: " % numguess)
            #    if not correct.startswith("y"):
            #        numguess = None


            if numguess is None:
                os.system(fname)
                num = raw_input("Number in image?: ")
                points = []
                for j in range(32):
                    x = random.randint(0, number.size[0]-1)
                    y = random.randint(0, number.size[1]-1)
                    color = pixels[x, y]
                    color = (color[0] << 16) + (color[1] << 8) + color[2]
                    points.append((x, y, color != 0xffffff))
                confirmed[num] = CaptchaNumber(num, points)
                imgdata[num] = number

                if len(confirmed) < 10:
                    confirmed_sorted = confirmed.keys()
                    confirmed_sorted.sort()
                    print "Confirmed numbers: %s" % confirmed_sorted
                else:
                    break

        if len(confirmed) == 10:
            print "All numbers collected. Creating html and java files."
            break


def create_html():
    print "Downloading 100 captchas for testing..."
    date = str(datetime.datetime.today())[:19]
    dir = os.path.join(os.getcwd(), date.replace("-","").replace(":","").replace(" ", ""))
    os.makedirs(dir)
    for i in range(100):
        f = open(os.path.join(dir, "captcha%03d.png" % (i+1)), "wb")
        r = urllib2.urlopen("https://mobil.nordea.se/banking-nordea/nordea-c3/captcha.png")
        f.write(r.read())
        f.close()
    print "Done."
    html = open("captchas_template.html", "r").read()
    html_item = open("captchas_item_template.html", "r").read()
    captchas = []
    for i in range(100):
        img = Image.open(os.path.join(dir, "captcha%03d.png" % (i+1)))
        captcha_item = html_item
        captcha_item = captcha_item.replace("%decaptcha%", numbers2text(img, extract_numbers(img))).replace("%number%", "%03d" % (i+1))
        captchas.append(captcha_item)
    html = html.replace("%captchas%", "\n".join(captchas)).replace("%date%", date)
    f = open(os.path.join(dir, "captchas.html"), "w")
    f.write(html)
    f.close()
    os.system(os.path.join(dir, "captchas.html"))
    javadata = []
    for x in range(len(confirmed.keys())):
        points = []
        captcha_number = confirmed[str(x)];
        for point in captcha_number.points:
            points.append((int(point[0]), int(point[1]), 1 if point[2] else 0))
        javadata.append(points)
    javadata = str(javadata).replace("[","{").replace("]","}").replace("(","{").replace(")","}")
    javadata = """
/**
 * Autogenerated captcha numbers for Nordea.
 *
 * @since %s
 */
public class CaptchaBreakerNumbers {
    public final static int[][][] NUMBERS = %s;
}
""" % (date, javadata)
    f = open(os.path.join(dir, "CaptchaBreakerNumbers.java"), "w")
    f.write(javadata)
    f.close()
    print "CaptchaBreakerNumbers.java generated."
    print "All done."

if __name__ == '__main__':
    main()
    create_html()