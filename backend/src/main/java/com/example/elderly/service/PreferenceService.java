package com.example.elderly.service;

import com.example.elderly.dto.TestPushDTO;
import com.example.elderly.dto.preference.PreferenceConfigDTO;

public interface PreferenceService {
    PreferenceConfigDTO getConfig();
    boolean saveConfig(PreferenceConfigDTO dto);
    boolean resetDefault();
    boolean testPush(TestPushDTO dto);
}
