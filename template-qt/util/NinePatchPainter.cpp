#include "NinePatchPainter.h"
#include <QPixmap>
#include <QList>
#include <QRect>
#include <QPainter>
#include <QPixmap>

/*-----------------------------------------------------------------------------|
 |                           NinePatchPainterPrivate                           |
 |----------------------------------------------------------------------------*/
class NinePatchPainterPrivate {
public:
    NinePatchPainterPrivate(const QPixmap &background,
                            int left, int top, int right, int bottom,
                            bool horizontalStretch, bool verticalStretch);

    // 根据九宫格 4 边的宽度把 rect 按九宫格分割为 9 个 rect: 左、左上、上、右上、右、右下、下、左下、中间
    QList<QRect> calculateNinePatchRects(const QRect &rect) const;

    // 对图片进行缩放
    QPixmap scalePixmap(const QPixmap &pixmap, const QSize &size) const;

public:
    int  left;   // 左边的宽
    int  top;    // 上边的宽
    int  right;  // 右边的宽
    int  bottom; // 下边的宽
    bool horizontalStretch; // 水平方向是否使用拉伸绘制
    bool verticalStretch;   // 垂直方向是否使用拉伸绘制

    QPixmap leftPixmap;        // 左边的子图
    QPixmap topLeftPixmap;     // 左上角的子图
    QPixmap topPixmap;         // 顶部的子图
    QPixmap topRightPixmap;    // 右上角的子图
    QPixmap rightPixmap;       // 右边的子图
    QPixmap bottomLeftPixmap;  // 左下角的子图
    QPixmap bottomPixmap;      // 底部的子图
    QPixmap bottomRightPixmap; // 右下角的子图
    QPixmap centerPixmap;      // 中间的子图
};

NinePatchPainterPrivate::NinePatchPainterPrivate(const QPixmap &background,
                                                 int left, int top, int right, int bottom,
                                                 bool horizontalStretch, bool verticalStretch)
    : left(left), top(top), right(right), bottom(bottom),
      horizontalStretch(horizontalStretch), verticalStretch(verticalStretch) {

    // 把 background 分割成 9 个子图，程序运行期间不会变，所以缓存起来
    QRect pixmapRect(0, 0, background.width(), background.height());
    QList<QRect> rects = calculateNinePatchRects(pixmapRect);

    leftPixmap        = background.copy(rects.at(0));
    topLeftPixmap     = background.copy(rects.at(1));
    topPixmap         = background.copy(rects.at(2));
    topRightPixmap    = background.copy(rects.at(3));
    rightPixmap       = background.copy(rects.at(4));
    bottomRightPixmap = background.copy(rects.at(5));
    bottomPixmap      = background.copy(rects.at(6));
    bottomLeftPixmap  = background.copy(rects.at(7));
    centerPixmap      = background.copy(rects.at(8));
}

QList<QRect> NinePatchPainterPrivate::calculateNinePatchRects(const QRect &rect) const {
    int x = rect.x();
    int y = rect.y();
    int cw = rect.width() - left - right;  // 中间部分的宽
    int ch = rect.height() - top - bottom; // 中间部分的高

    // 根据把 rect 分割成 9 个部分: 左、左上、上、右上、右、右下、下、左下、中间
    QRect leftRect(x, y + top, left, ch);
    QRect topLeftRect(x, y, left, top);
    QRect topRect(x + left, y, cw, top);
    QRect topRightRect(x + left + cw, y, right, top);
    QRect rightRect(x + left + cw, y + top, right, ch);
    QRect bottomRightRect(x + left + cw, y + top + ch, right, bottom);
    QRect bottomRect(x + left, y + top + ch, cw, bottom);
    QRect bottomLeftRect(x, y + top + ch, left, bottom);
    QRect centerRect(x + left, y + top, cw, ch);

    return QList<QRect>() << leftRect << topLeftRect
                          << topRect << topRightRect << rightRect
                          << bottomRightRect << bottomRect << bottomLeftRect
                          << centerRect;
}

QPixmap NinePatchPainterPrivate::scalePixmap(const QPixmap &pixmap, const QSize &size) const {
    // 缩放时忽略图片的高宽比，使用平滑缩放的效果
    return pixmap.scaled(size, Qt::IgnoreAspectRatio, Qt::SmoothTransformation);
}

/*-----------------------------------------------------------------------------|
 |                              NinePatchPainter                               |
 |----------------------------------------------------------------------------*/
NinePatchPainter::NinePatchPainter(const QPixmap &background,
                                   int left, int top, int right, int bottom,
                                   bool horizontalStretch, bool verticalStretch)
    : d(new NinePatchPainterPrivate(background, left, top, right, bottom, horizontalStretch, verticalStretch)) {
}

NinePatchPainter::~NinePatchPainter() {
    delete d;
}


void NinePatchPainter::paint(QPainter *painter, const QRect &rect) const {
    // 把要绘制的 Rect 分割成 9 个部分，上，右，下，左 4 边的宽和背景图的一样
    QList<QRect> rects = d->calculateNinePatchRects(rect);

    QRect leftRect        = rects.at(0);
    QRect topLeftRect     = rects.at(1);
    QRect topRect         = rects.at(2);
    QRect topRightRect    = rects.at(3);
    QRect rightRect       = rects.at(4);
    QRect bottomRightRect = rects.at(5);
    QRect bottomRect      = rects.at(6);
    QRect bottomLeftRect  = rects.at(7);
    QRect centerRect      = rects.at(8);

    // 绘制 4 个角
    painter->drawPixmap(topLeftRect,     d->topLeftPixmap);
    painter->drawPixmap(topRightRect,    d->topRightPixmap);
    painter->drawPixmap(bottomRightRect, d->bottomRightPixmap);
    painter->drawPixmap(bottomLeftRect,  d->bottomLeftPixmap);

    // 绘制左、右边
    if (d->horizontalStretch) {
        // 水平拉伸
        painter->drawPixmap(leftRect,  d->scalePixmap(d->leftPixmap,  leftRect.size()));
        painter->drawPixmap(rightRect, d->scalePixmap(d->rightPixmap, rightRect.size()));
    } else {
        // 水平平铺
        painter->drawTiledPixmap(leftRect,  d->leftPixmap);
        painter->drawTiledPixmap(rightRect, d->rightPixmap);
    }

    // 绘制上、下边
    if (d->verticalStretch) {
        // 垂直拉伸
        painter->drawPixmap(topRect,    d->scalePixmap(d->topPixmap,    topRect.size()));
        painter->drawPixmap(bottomRect, d->scalePixmap(d->bottomPixmap, bottomRect.size()));
    } else {
        // 垂直平铺
        painter->drawTiledPixmap(topRect,    d->topPixmap);
        painter->drawTiledPixmap(bottomRect, d->bottomPixmap);
    }

    int pmw = d->centerPixmap.width();
    int pmh = d->centerPixmap.height();
    int crw = centerRect.width();
    int crh = centerRect.height();

    // 绘制中间部分(最简单办法就是中间部分都进行拉伸)
    if (d->horizontalStretch && d->verticalStretch) {
        // 水平和垂直都拉伸
        painter->drawPixmap(centerRect, d->scalePixmap(d->centerPixmap, centerRect.size()));
    } else if (d->horizontalStretch && !d->verticalStretch) {
        // 水平拉伸，垂直平铺
        if (crh % pmh != 0) {
            pmh = ((float)crh) / (crh/pmh+1);
        }
        QSize size(crw, pmh);
        QPixmap centerPixmap = d->scalePixmap(d->centerPixmap, size);
        painter->drawTiledPixmap(centerRect, centerPixmap);
    } else if (!d->horizontalStretch && d->verticalStretch) {
        // 水平平铺，垂直拉伸
        if (crw % pmw != 0) {
            pmw = ((float)crw) / (crw/pmw+1);
        }
        QSize size(pmw, crh);
        QPixmap centerPixmap = d->scalePixmap(d->centerPixmap, size);
        painter->drawTiledPixmap(centerRect, centerPixmap);
    } else {
        // 水平和垂直都平铺
        painter->drawTiledPixmap(centerRect, d->centerPixmap);
    }
}
