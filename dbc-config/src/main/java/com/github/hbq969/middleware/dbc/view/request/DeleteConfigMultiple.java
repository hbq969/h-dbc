package com.github.hbq969.middleware.dbc.view.request;

import com.github.hbq969.middleware.dbc.model.AccountServiceProfile;
import lombok.Data;

import java.util.List;

@Data
public class DeleteConfigMultiple {
    private AccountServiceProfile asp;
    private List<String> configKeys;
}
