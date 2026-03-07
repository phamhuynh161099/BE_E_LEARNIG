package com.sakata.boilerplate.mapper.primary;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import com.sakata.boilerplate.models.TrackingVideo;

@Mapper
public interface TrackingVideoMapper {
    int updateDynamic(TrackingVideo trackingVideo);

    @Insert("INSERT INTO tracking_video (video_id, job_id, status, percentage, resolution,total_duration) VALUES (#{videoId},#{jobId}, #{status}, #{percentage}, #{resolution}, #{totalDuration})")
    int insertInitialTrackingVideo(TrackingVideo trackingVideo);
}
