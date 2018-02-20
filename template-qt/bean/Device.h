#ifndef DEVICE_H
#define DEVICE_H
#include <QString>

/**
 * 传感器
 */
class Device {
public:
    Device();
    Device(const QString &id, const QString &type, const QString &address, int voltage, bool working);

    QString toString() const;

    QString id;   // 记录器 ID
    QString type; // 类型: T, H, P
    QString address;
    int     voltage = 0;     // 电压
    bool    working = false; // 工作状态: 工作中为 true，否则为 false
};

#endif // DEVICE_H
