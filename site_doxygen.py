import re
import sys


def add_clsdata(clsdata, strs):
    strs = strs.split('.')
    for str in strs:
        if str not in clsdata:
            clsdata[str] = {}
        clsdata = clsdata[str]


def write_clsdata(f, index, index2, clsdata):
    old = -1
    for k, v in clsdata.items():
        index2 = index2 + 1
        if len(v) == 0:
            f.write(" " * index * 4)
            f.write("node" + str(index2) + " [label=\"{" + k + "}\", shape=record];\n")
            if 0 <= old:
                f.write(f"node{old}->node{index2}[style=invis]\n")
            old = index2
        else:
            f.write(" " * index * 4)
            f.write("subgraph cluster_" + str(index2) + " {\n")
            f.write(" " * (index + 1) * 4)
            f.write(f"label = \"{k}\"\n")
            index2 = write_clsdata(f, index + 1, index2, v)
            f.write(" " * index * 4)
            f.write("}\n")
    return index2


base = None
project_name = ""
with open('site_doxygen.yml', 'r', encoding='utf-8') as f:
    for line in f:
        match = re.search('project_output_dir\\s*:\\s*\"([^\"]+)', line)
        if match:
            base = match.group(1)
        match = re.search('project_name\\s*:\\s*\"([^\"]+)', line)
        if match:
            project_name = match.group(1)

if base is None:
    sys.exit(-1)

uml = base + "/html/annotated.html"

urls = []
with open(uml, 'r', encoding='utf-8') as f:
    for line in f:
        match = re.search('<a\\s+class="([^\"]+)\"\\s+href=\"(class[^\"]+)', line)
        if match:
            urls.append(match.group(2))

imgs = []
clsdata = {}
for url in urls:
    url = base + "/html/" + url
    with open(url, 'r', encoding='utf-8') as f:
        title = ""
        for line in f:
            match = re.search('<div\\s+class=\"title\">([^<>&\\s]+)', line)
            if match:
                add_clsdata(clsdata, match.group(1))
            match = re.search('^(Inheritance|Collaboration)\\s+[^<>]+', line)
            if match:
                title = match.group()
            match = re.search('<img\\s+src=\"([^\"]+)', line)
            if match:
                imgs.append({"title": title, "utl": match.group(1)})
                break

uml = base + "/html/index2.html"
with open(uml, mode='w', encoding="utf-8") as f:
    f.write("<html lang=\"jp\">\n")
    f.write("<head>\n")
    f.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n")
    f.write("</head>\n")
    f.write("<body>\n")
    f.write(f"<H1>{project_name}</H1>")
    for key in imgs:
        f.write(f"<H2>{key["title"]}</H2>")
        f.write(f"<img src=\"{key["utl"]}\" />\n")  # height=\"100%\"
        f.write("<mbp:pagebreak/>\n")
    f.write("</body>\n")
    f.write("</html>\n")


uml = base + "/html/index.dot"
with open(uml, mode='w', encoding="utf-8") as f:
    f.write("digraph G {\n")
    write_clsdata(f, 1, 0, clsdata)
    f.write("}\n")

#
