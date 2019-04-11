include(../common.pri)

QT      -= gui
TARGET   = driver
TEMPLATE = lib

LIBS += -L$$bin -lutil
