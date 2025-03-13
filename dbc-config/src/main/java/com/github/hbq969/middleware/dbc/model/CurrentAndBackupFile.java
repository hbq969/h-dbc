package com.github.hbq969.middleware.dbc.model;

import lombok.Data;

@Data
public class CurrentAndBackupFile {
    // 当前配置
    private String currentFileContent;

    // 待还原配置
    private String backupFileContent;
}
