package com.sakata.boilerplate.mapper.primary;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

import com.sakata.boilerplate.models.Video;
import com.sakata.boilerplate.models.vo.TrackingVideoVO;

@Mapper
public interface VideoMapper {

    Optional<Video> findById(Long id);

    @Insert("INSERT INTO videos (unique_file_name, original_file_name, original_path, file_size, status) VALUES (#{uniqueFileName}, #{originalFileName}, #{originalPath}, #{fileSize}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int saveInitializeVideo(Video video);

    @Update("<script>" +
            "UPDATE videos" +
            "<set>" +
            "  <if test='originalFileName != null'>original_file_name = #{originalFileName},</if>" +
            "  <if test='originalPath != null'>original_path = #{originalPath},</if>" +
            "  <if test='fileSize != null'>file_size = #{fileSize},</if>" +
            "  <if test='encoded720Path != null'>encoded_720p_path = #{encoded720Path},</if>" +
            "  <if test='encoded1080Path != null'>encoded_1080p_path = #{encoded1080Path},</if>" +
            "  <if test='duration != null'>duration = #{duration},</if>" +
            "  <if test='status != null'>status = #{status},</if>" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int updateVideoDynamic(Video video);


    // List<Video> findAll();
    List<TrackingVideoVO> findAll();
}
