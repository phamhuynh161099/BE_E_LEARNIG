package com.sakata.boilerplate.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sakata.boilerplate.mapper.primary.TrackingVideoMapper;
import com.sakata.boilerplate.models.TrackingVideo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true, transactionManager = "primaryTransactionManager")
public class TrackingVideoService {
    private final TrackingVideoMapper trackingVideoMapper;

    @Transactional(transactionManager = "primaryTransactionManager")
    public Object initialTrackingVideo(TrackingVideo req) {

        System.out.println(">>>>initialTrackingVideo"+req.getVideoId());
        var affectRowInsert = trackingVideoMapper.insertInitialTrackingVideo(req);
        return affectRowInsert;
    }

    @Transactional(transactionManager = "primaryTransactionManager")
    public Object updateDymamic(TrackingVideo req) {

        System.out.println(">>>>initialTrackingVideo"+req.getVideoId());
        var affectRowInsert = trackingVideoMapper.updateDynamic(req);
        return affectRowInsert;
    }
}
