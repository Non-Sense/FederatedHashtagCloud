import requests
from wordcloud import WordCloud
url = "http://localhost:8080"
res = requests.get(url+"/api/v1/tags")
dat = {}
for x in res.json():
    dat[x["name"]] = x["count"]
wc = WordCloud(
    font_path="C:\\Windows\\Fonts\\BIZ-UDGothicR.ttc",
    width=1400,
    background_color="rgba(255, 255, 255, 0)",
    mode="RGBA",
    height=1400).generate_from_frequencies(dat)

with open("./frontend/public/wd.svg", "w", encoding="utf-8") as f:
    f.write(wc.to_svg())