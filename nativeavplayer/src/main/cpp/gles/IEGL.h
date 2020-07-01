//
// Created by 阳坤 on 2020-05-23.
//

#ifndef IKAVEDIT_IEGL_H
#define IKAVEDIT_IEGL_H


class IEGL {
public:
    virtual bool init(void *win) = 0;

    virtual void close() = 0;

    virtual void draw() = 0;




protected:
    IEGL() {}
};


#endif //IKAVEDIT_IEGL_H
