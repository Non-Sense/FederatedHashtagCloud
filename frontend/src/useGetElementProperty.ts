// 引数のtargetProperty をDOMRectのもつPropertyに限定する
import {RefObject, useCallback} from "react";

type DOMRectProperty = keyof Omit<DOMRect, 'toJSON'>;

// RefObjectの型は div, span, p, input などのさまざまなHTML要素に対応できるようにextendsで制限をかけつつ抽象化
export const useGetElementProperty = <T extends HTMLElement>(
    elementRef: RefObject<T>
) => {
    const getElementProperty = useCallback(
        (targetProperty: DOMRectProperty): number => {
            const clientRect = elementRef.current?.getBoundingClientRect();
            if (clientRect) {
                return clientRect[targetProperty];
            }

            // clientRect が undefined のときはデフォルトで0を返すようにする
            return 0;
        },
        [elementRef]
    );

    return {
        getElementProperty,
    };
};