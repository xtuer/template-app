#include "Device.h"

Device::Device() : Device("", "", "", 0, false) {

}

Device::Device(const QString &id, const QString &type, const QString &address, int voltage, bool working) {
    this->id = id;
    this->type = type;
    this->address = address;
    this->voltage = voltage;
    this->working = working;
}

QString Device::toString() const {
    return QString("ID: %1, Type: %2, Addres: %3, Voltage: %4, Working: %5")
            .arg(id).arg(type).arg(address).arg(voltage).arg(working);
}
