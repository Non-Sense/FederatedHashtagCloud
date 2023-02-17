import requests
from wordcloud import WordCloud
import re
import json

url = "http://localhost:8080"
res = requests.get(url + "/api/v1/tags")
dat = {}

height = 1400
width = 1400
hostname = res.json()["host_name"]
for x in res.json()["tags"]:
    dat[x["name"]] = x["count"]

wc = WordCloud(
    font_path="C:\\Windows\\Fonts\\BIZ-UDGothicR.ttc",
    width=height,
    background_color="rgba(255, 255, 255, 0)",
    mode="RGBA",
    height=width).generate_from_frequencies(dat)

exp = r'^<text transform="translate\((\d+),(\d+)\)( rotate\((-?\d*)\))?" font-size="(\d+)" style="fill:rgb\((\d+), (\d+), (\d+)\)">(.*)<\/text>$'
rep = re.compile(exp)
svg = wc.to_svg().splitlines()
l = []
for s in svg:
    res = rep.match(s)
    if res is not None:
        g = res.groups()
        rt = 0
        if g[3] is not None:
            rt = int(g[3])
        t = {
            "cnt": dat[g[8]],
            "t": {"x": int(g[0]), "y": int(g[1])},
            "r": rt,
            "s": int(g[4]),
            "col": {"r": int(g[5]), "g": int(g[6]), "b": int(g[7])},
            "st": g[8]
        }
        l.append(t)

js = {
    "width": width,
    "height": height,
    "data": l
}

print(json.dumps(js, ensure_ascii=False))
