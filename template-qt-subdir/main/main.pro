include(../common.pri)

QT       += core gui widgets
TARGET   = main
TEMPLATE = app

LIBS += \
    -L$$bin -lutil \
    -L$$bin -lbean \
    -L$$bin -ldao \
    -L$$bin -lgui-business

SOURCES += \
    main.cpp
