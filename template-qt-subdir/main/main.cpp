#include <QApplication>
#include <QDebug>

int main(int argc, char *argv[]) {
    QApplication app(argc, argv);

    qDebug() << "Start...";

    return app.exec();
}
