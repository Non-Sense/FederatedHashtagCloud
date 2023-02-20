import datetime
import random
import requests
from wordcloud import WordCloud
import re
import json
import matplotlib.cm as cm
import matplotlib.colors as mcolors
import sys


def get_tag_data(url):
    res = requests.get(url + "/api/v1/tags")
    dat = {}
    hostname = res.json()["hostname"]
    for x in res.json()["tags"]:
        dat[x["name"]] = x["count"]
    return hostname, dat


cmap = cm.get_cmap("tab10")


def color_func(word, font_size, position, orientation, random_state=None, **kwargs):
    while True:
        i = random.randint(0, 9)
        if i != 7:
            break
    return mcolors.rgb2hex(cmap(i))


def make_wordcloud_svg(dat, height, width, font):
    wc = WordCloud(
        font_path=font,
        width=width,
        background_color="rgba(255, 255, 255, 0)",
        mode="RGBA",
        color_func=color_func,
        height=height).generate_from_frequencies(dat)
    return wc.to_svg()


def svg_string_to_json(svg, dat):
    exp = r'^<text transform="translate\((\d+),(\d+)\)( rotate\((-?\d*)\))?" font-size="(\d+)" style="fill:#(.+)">(.*)<\/text>$'
    rep = re.compile(exp)
    l = []
    for s in svg:
        res = rep.match(s)
        if res is not None:
            g = res.groups()
            rt = 0
            if g[3] is not None:
                rt = int(g[3])
            t = {
                "c": dat[g[6]],  # count
                "x": int(g[0]),  # x pos
                "y": int(g[1]),  # y pos
                "e": rt,  # rotate
                "s": int(g[4]),  # size
                "l": g[5],  # color(http color)
                "t": g[6]  # text
            }
            l.append(t)
    return sorted(l, key=lambda x: x["c"], reverse=True)


def main(font, url, output):
    start = datetime.datetime.now(datetime.timezone.utc).isoformat()
    hostname, dat = get_tag_data(url)
    height = 1500
    width = 1500
    svg = make_wordcloud_svg(dat, height, width, font)
    
    svg_json = svg_string_to_json(svg.splitlines(), dat)
    js = {
        "time": start,
        "hostname": hostname,
        "width": width,
        "height": height,
        "data": svg_json
    }

    with open(output, "w", encoding="UTF-8") as f:
        f.write(json.dumps(js, ensure_ascii=False))


if __name__ == '__main__':
    args = sys.argv
    main(args[1], args[2], args[3])
    exit(0)
