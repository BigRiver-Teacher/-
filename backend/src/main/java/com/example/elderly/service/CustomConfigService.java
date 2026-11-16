package com.example.elderly.service;

import com.example.elderly.dto.custom.DashboardConfigDTO;
import com.example.elderly.dto.custom.MobileConfigDTO;
import com.example.elderly.dto.custom.SidebarConfigDTO;

public interface CustomConfigService {
    SidebarConfigDTO getSidebarConfig();

    void saveSidebarConfig(SidebarConfigDTO dto);
    void resetSidebar();

    DashboardConfigDTO getDashboardConfig();
    void saveDashboardConfig(DashboardConfigDTO dto);
    void resetDashboard();

    MobileConfigDTO getMobileConfig();
    void saveMobileConfig(MobileConfigDTO dto);
    void resetMobile();
}
