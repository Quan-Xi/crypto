package com.flow.mconfig.mapper;

import org.apache.ibatis.annotations.*;

/**
 * @author wangqiyun
 *  2019-11-13 17:33
 */

@Mapper
public interface ConfigMapper {
    @Select("SELECT * FROM `config` WHERE `name`=#{name}")
    Config get(String name);

    @Insert("REPLACE INTO `config`(`name`, `value`) VALUES (#{name},#{value})")
    long insert(Config config);

    @Insert("INSERT INTO `config`(`name`, `value`) VALUES (#{name},#{value})")
    long insertReal(Config config);

    @Update("UPDATE `config` SET `value`=#{newValue} WHERE `name`=#{name} AND `value`=#{oldValue}")
    long update(@Param("name") String name, @Param("oldValue") String oldValue, @Param("newValue") String newValue);

    @Update("REPLACE INTO `config` (`name`, `value`) VALUES (#{name}, #{value}) ")
    long updateParam(@Param("name") String name, @Param("value") String value);

    @Update("UPDATE `config` SET `value`=`value` + #{value} WHERE `name`=#{name}")
    long incrementVal(@Param("name") String name, @Param("value") String value);

    @Select("SELECT `value` FROM `config` WHERE `name` = #{name}")
    String getParam(@Param("name") String name);

}
