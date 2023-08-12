package com.process.service;

import com.process.DTO.RejectBean;
import com.process.util.Result;

public interface Process {

    Result rejectToLastNode(RejectBean rejectBean);

    Result rejectToFirstNode(RejectBean rejectBean);

}
