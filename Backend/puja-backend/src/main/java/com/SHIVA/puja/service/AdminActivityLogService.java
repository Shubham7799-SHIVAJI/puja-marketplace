package com.SHIVA.puja.service;

import java.util.List;

import com.SHIVA.puja.entity.AdminActivityLog;

public interface AdminActivityLogService {

    void log(String actionType, String targetEntity, Long targetId, String details);

    List<AdminActivityLog> listRecent();
}
