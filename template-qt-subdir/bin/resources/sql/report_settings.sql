<?xml version="1.0" encoding="UTF-8"?>
<!--
DROP TABLE IF EXISTS report_settings;
CREATE TABLE report_settings (
    id integer primary key autoincrement,
    device_type integer unique,
    device_type_name text,
    settings text
);

INSERT OR REPLACE INTO report_settings (device_type, device_type_name, settings) VALUES (1, '蒸汽灭菌设备', '{yyyy}');
-->

<sqls namespace="ReportSettings">
	<!-- 插入或则更新报表配置 -->
    <sql id="insertOrUpdateReportSettings">
        INSERT OR REPLACE INTO report_settings (device_type, device_type_name, settings) VALUES (:deviceType, :deviceTypeName, :settings);
    </sql>

	<!-- 使用设备类型查询配置 -->
    <sql id="findReportSettingsByDeviceType">
        SELECT device_type AS deviceType, device_type_name AS deviceTypeName, settings FROM report_settings WHERE device_type=:deviceType
    </sql>
</sqls>
