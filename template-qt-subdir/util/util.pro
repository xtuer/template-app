include(../common.pri)

QT      += core gui widgets
TARGET   = util
TEMPLATE = lib

HEADERS += \
    Config.h \
    Json.h \
    LogHandler.h \
    sensorcolor.h \
    Singleton.h \
    UiUtil.h \
    utils.h

SOURCES += \
    Config.cpp \
    Json.cpp \
    LogHandler.cpp \
    sensorcolor.cpp \
    UiUtil.cpp
