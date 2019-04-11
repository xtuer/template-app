include(../common.pri)

QT      += core gui widgets serialport printsupport svg axcontainer sql xml charts opengl
TARGET   = gui-business
TEMPLATE = lib

LIBS += -lOpengl32
LIBS += -L$$bin -lutil
LIBS += -L$$bin -lgui-component
