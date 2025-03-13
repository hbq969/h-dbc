package com.github.hbq969.middleware.dbc.view.request;

import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import lombok.Data;

@Data
public class CompareCurrentAndBackupFile {
    private AccountServiceProfile current;
    private String backupId;
}
