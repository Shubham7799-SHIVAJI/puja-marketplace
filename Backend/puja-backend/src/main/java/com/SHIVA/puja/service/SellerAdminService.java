package com.SHIVA.puja.service;

import com.SHIVA.puja.dto.SellerOnboardingRequest;
import com.SHIVA.puja.dto.SellerOnboardingResponse;

public interface SellerAdminService {

    SellerOnboardingResponse onboardSeller(SellerOnboardingRequest request);
}
