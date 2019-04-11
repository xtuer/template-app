#ifndef SENSORCOLOR_H
#define SENSORCOLOR_H

#include <QList>
#include <QColor>

class SensorColor
{
public:
    SensorColor();
    QColor getColor(int);


private:
    QList<QColor> colorList;
};

#endif // SENSORCOLOR_H
