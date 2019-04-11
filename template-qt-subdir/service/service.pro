include(../common.pri)

QT      -= gui
TARGET   = service
TEMPLATE = lib

LIBS += -L$$bin -lutil
