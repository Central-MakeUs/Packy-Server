package com.dilly.admin.dao;

import com.dilly.admin.domain.setting.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingRepository extends JpaRepository<Setting, Long> {

}
