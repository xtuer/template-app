include(../common.pri)

QT      -= gui
TARGET   = bean
TEMPLATE = lib

LIBS += -L$$bin -lutil

HEADERS += \
    Permission.h \
    User.h

SOURCES += \
    Permission.cpp \
    User.cpp
