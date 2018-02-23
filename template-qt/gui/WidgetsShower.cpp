#include "ui_WidgetsShower.h"
#include "WidgetsShower.h"
#include "MessageBox.h"

#include <QDebug>
#include <QStringListModel>
#include <QStandardItemModel>

WidgetsShower::WidgetsShower(QWidget *parent) : QWidget(parent), ui(new Ui::WidgetsShower) {
    ui->setupUi(this);
    setAttribute(Qt::WA_StyledBackground);

    QStringList list;
    list << "北京" << "天津" << "河北" << "山西" << "辽宁" << "吉林" << "黑龙江" << "上海" << "江苏" << "浙江" << "湖南" << "海南";

    // 添加 List Items
    QStringListModel *listModel = new QStringListModel();
    listModel->setStringList(list);
    ui->listView->setModel(listModel);

    // 添加 Table Items
    int rowCount = 25;
    int colCount = 3;
    QStandardItemModel *tableModel = new QStandardItemModel(rowCount, colCount, this); // 2 Rows and 3 Columns
    tableModel->setHorizontalHeaderLabels(QStringList() << "省" << "市" << "县");
    ui->tableView->setModel(tableModel);
    ui->tableView->setSelectionBehavior(QAbstractItemView::SelectRows);
    ui->tableView->horizontalHeader()->setStretchLastSection(true);

    for (int row = 0; row < rowCount; ++row) {
        for (int col = 0; col < colCount; ++col) {
            QString text = list.at((row + col) % list.size());
            QStandardItem *item = new QStandardItem(text);
            tableModel->setItem(row, col, item);

            if (col == 0) {
                item->setCheckable(true);
                item->setCheckState(row % 2 == 0 ? Qt::Checked : Qt::Unchecked);
            } else if (col == 2) {
                item->setIcon(QIcon("image/common/close.png"));
            }
        }
    }

    connect(ui->horizontalSlider, &QSlider::valueChanged, ui->progressBar, &QProgressBar::setValue);

    connect(ui->confirmButton, &QPushButton::clicked, [this] {
        // 信息使用 HTML
        QString info = "<b>公司</b>: 花果山再来一瓶科技信息技术有限公司<br>"
                       "<b>法人</b>: 齐天大圣<br>"
                       "<b>版本</b>: Release 1.1.3<br>"
                       "<center><img src=':/image/top-window/logo.png' width=64 height=64></center>";
        bool result = MessageBox::confirm(info, 350, 140);
        qDebug() << "Confirm:" << result;
    });

    connect(ui->messageButton, &QPushButton::clicked, [this] {
        MessageBox::message("简单消息对话框");
    });
}

WidgetsShower::~WidgetsShower() {
    delete ui;
}
