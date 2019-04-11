include(../common.pri)

QT      += sql xml
QT      -= gui
TARGET   = dao
TEMPLATE = lib

LIBS += -L$$bin -lutil
LIBS += -L$$bin -lbean

include(db/db.pri)

HEADERS += \
    UserDao.h

SOURCES += \
    UserDao.cpp
