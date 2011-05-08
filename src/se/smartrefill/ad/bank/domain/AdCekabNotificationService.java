package se.smartrefill.ad.bank.domain;

import java.util.ArrayList;
import java.util.List;
import se.smartrefill.ad.domain.AdService;

public class AdCekabNotificationService extends AdService {
	private static final long serialVersionUID = 1L;
	private List<AdCekabPan> pans;

	public AdCekabNotificationService() {
		this.pans = new ArrayList<AdCekabPan>();
	}

	public List<AdCekabPan> getPans() {
		return pans;
	}

}
