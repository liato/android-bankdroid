package se.smartrefill.ad.domain;

import java.io.Serializable;

public abstract class AdService
  implements Serializable
{
  private static final long serialVersionUID = 1L;
  private Integer id;
  private String imei;
  private String manufacturer;
  private String mobileNumber;
  private String model;
  private String operator;
  private String password;
  private String userName;

  public Integer getId()
  {
    return this.id;
  }

  public String getImei()
  {
    return this.imei;
  }

  public String getManufacturer()
  {
    return this.manufacturer;
  }

  public String getMobileNumber()
  {
    return this.mobileNumber;
  }

  public String getModel()
  {
    return this.model;
  }

  public String getOperator()
  {
    return this.operator;
  }

  public String getPassword()
  {
    return this.password;
  }

  public String getUserName()
  {
    return this.userName;
  }
}