#ifndef NINEPATCHPAINTER_H
#define NINEPATCHPAINTER_H

class QRect;
class QString;
class QPainter;
class NinePatchPainterPrivate;

/**
 * @brief 使用九宫格的方式绘图
 */
class NinePatchPainter {
public:
    NinePatchPainter(int left, int top, int right, int bottom, const QString &imagePath, bool tiled);
    ~NinePatchPainter();

    void draw(QPainter *painter, const QRect &rect) const;

private:
    NinePatchPainterPrivate *d;
};

#endif // NINEPATCHPAINTER_H
