import * as React from 'react';
import {useEffect, useRef} from 'react';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import CssBaseline from '@mui/material/CssBaseline';
import Drawer from '@mui/material/Drawer';
import IconButton from '@mui/material/IconButton';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import MenuIcon from '@mui/icons-material/Menu';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import useAppBarHeight from "./useAppBarHeight";
import * as d3 from "d3";
import {TransformComponent, TransformWrapper} from "react-zoom-pan-pinch";
import {GetWordCloud} from "./api/GetWordCloud";
import GeneratedWordCloudApiResponse from "./vo/GeneratedWordCloudApiResponse";
import {Container, Dialog, Divider} from "@mui/material";
import {GitHub, HelpOutline, Twitter} from "@mui/icons-material";

require("./tagdrawer.css")

const drawerWidth = 240;
const svgPadding = 200;

interface Props {
    window?: () => Window;
}

class SvgSize {
    height: number;
    width: number;

    constructor(height: number, width: number) {
        this.height = height;
        this.width = width;
    }
}

let isDragging = false;
let draggingResetId: number | null = null;

function openTagPage(hostName: string, name: string) {
    if (isDragging)
        return;
    window.open("https://" + hostName + "/tags/" + name);
}

function drawSvg(data: GeneratedWordCloudApiResponse, svg: SVGSVGElement) {
    const sel = d3.select(svg);

    data.data.forEach(elm => {
        sel
            .append("text")
            .attr("transform", `translate(${elm.x + svgPadding},${elm.y + svgPadding}) rotate(${elm.e})`)
            .attr("font-size", elm.s)
            .attr("style", `fill:#${elm.l}`)
            .text(elm.t)
            .on("click", () => {
                openTagPage(data.hostname, elm.t);
            })
    })

}

function getFormattedDate(date: Date): string {
    return `${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getDate().toString().padStart(2, '0')} 
    ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

export default function TagDrawer(props: Props) {
    const {window} = props;
    const [mobileOpen, setMobileOpen] = React.useState(false);
    const [tags, setTags] = React.useState<GeneratedWordCloudApiResponse>(new GeneratedWordCloudApiResponse("", "", 1500, 1500, []));
    const [svgSize, setSvgSize] = React.useState<SvgSize>(new SvgSize(1500, 1500))
    const [updateTime, setUpdateTime] = React.useState("")
    const [helpOpen, setHelpOpen] = React.useState(false);

    const svgRef = useRef<SVGSVGElement>(null);

    const appBarHeight = useAppBarHeight()

    const handleDrawerToggle = () => {
        setMobileOpen(!mobileOpen);
    };

    useEffect(() => {
        let ignore = false;
        if (ignore)
            return;

        GetWordCloud(window ? window().location.origin : "http://localhost:8080", data => {
            setSvgSize(new SvgSize(data.height, data.width));
            setTags(data);
            const time = new Date(Date.parse(data.time));
            setUpdateTime(getFormattedDate(time))
            if (svgRef.current != null) {
                drawSvg(data, svgRef.current);
            }
        })

        return () => {
            ignore = true;
        }
    }, [])

    const drawer = (
        <div>
            <Toolbar>
                updated at {updateTime}
            </Toolbar>
            <Divider/>
            <List>
                {tags.data.map(data => (
                    <ListItem disablePadding key={data.t}>
                        <ListItemButton onClick={() => openTagPage(tags.hostname, data.t)}>
                            <Box>
                                <Typography variant={"h6"}>
                                    {data.t}
                                </Typography>
                                <Box sx={{fontSize: {xs: "0.7rem"}, color: {xs: "text.disabled"}}}>
                                    by {data.c} people
                                </Box>
                            </Box>
                        </ListItemButton>
                    </ListItem>
                ))}
            </List>
        </div>
    );

    const container = window !== undefined ? () => window().document.body : undefined;

    return (
        <Box sx={{display: 'flex'}}>
            <CssBaseline/>
            <Dialog open={helpOpen} onClose={() => setHelpOpen(false)}>
                <Box padding={"1rem"}>
                    <Container>
                        <img src={"banner.png"} style={{width: "100%"}} alt={"Otadon Hashtag Cloud"}/>
                        <Typography variant="body1" component="div">
                            連合タイムラインで言及されたハッシュタグをワードクラウドで表示します。<br/>
                            定期的に集計され、期間内に対象のハッシュタグを言及したアカウント数がカウントされます。<br/>
                            ハッシュタグをクリックすると投稿を確認できます。<br/>
                            <br/>
                            お問い合わせやご要望はこちらへ<br/>
                            <IconButton href={"https://otadon.com/@N0n5ense"}>
                                <img height={"24px"} width={"24px"} src={"mastodon-logo.svg"} alt={"mastodon icon"}/>
                            </IconButton>
                            <IconButton href={"https://twitter.com/N0n5ense"}>
                                <Twitter/>
                            </IconButton>
                            <IconButton href={"https://github.com/Non-Sense/FederatedHashtagCloud"}>
                                <GitHub/>
                            </IconButton>
                        </Typography>
                    </Container>
                </Box>
            </Dialog>
            <AppBar
                position="fixed"
                sx={{
                    width: {sm: `calc(100% - ${drawerWidth}px)`},
                    ml: {sm: `${drawerWidth}px`},
                }}
            >
                <Toolbar>
                    <IconButton
                        color="inherit"
                        aria-label="open drawer"
                        edge="start"
                        onClick={handleDrawerToggle}
                        sx={{mr: 2, display: {sm: 'none'}}}
                    >
                        <MenuIcon/>
                    </IconButton>
                    <img src={"banner_white.png"} style={{height: appBarHeight, padding: "10px"}}
                         alt={"Otadon Hashtag Cloud"}/>
                    <Box sx={{flexGrow: 1}}/>
                    <IconButton onClick={() => setHelpOpen(true)} aria-label={"open help"}>
                        <HelpOutline/>
                    </IconButton>
                </Toolbar>
            </AppBar>
            <Box
                component="nav"
                sx={{width: {sm: drawerWidth}, flexShrink: {sm: 0}}}
            >
                {/* The implementation can be swapped with js to avoid SEO duplication of links. */}
                <Drawer
                    container={container}
                    variant="temporary"
                    open={mobileOpen}
                    onClose={handleDrawerToggle}
                    ModalProps={{
                        keepMounted: true, // Better open performance on mobile.
                    }}
                    sx={{
                        display: {xs: 'block', sm: 'none'},
                        '& .MuiDrawer-paper': {boxSizing: 'border-box', width: drawerWidth},
                    }}
                >
                    {drawer}
                </Drawer>
                <Drawer
                    variant="permanent"
                    sx={{
                        display: {xs: 'none', sm: 'block'},
                        '& .MuiDrawer-paper': {boxSizing: 'border-box', width: drawerWidth},
                    }}
                    open
                >
                    {drawer}
                </Drawer>
            </Box>
            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    width: {sm: `calc(100% - ${drawerWidth}px)`},
                    height: {xs: `100vh`},
                    overflow: {xs: "hidden"}
                }}
            >
                <Toolbar/>
                <Box sx={{width: {sm: `100%`}, height: {xs: `calc(100vh - ${appBarHeight}px)`}, p: 0, m: 0}}>
                    <TransformWrapper initialScale={0.6} limitToBounds={true} minScale={0.2} disablePadding={false}
                                      centerOnInit={true} onPanningStop={(_, event) => {
                        if (event instanceof MouseEvent) {
                            if (window) {
                                draggingResetId = window().setTimeout(() => {
                                    isDragging = false;
                                    draggingResetId = null;
                                }, 100);
                            } else {
                                isDragging = false;
                            }
                        }
                    }} onPanning={(_, event) => {
                        if (event instanceof MouseEvent) {
                            if (draggingResetId !== null)
                                window?.().clearTimeout(draggingResetId);
                            isDragging = true;
                        }
                    }}>
                        <TransformComponent wrapperStyle={{height: `calc(100vh - ${appBarHeight}px)`, width: `100%`}}>
                            <svg ref={svgRef} width={svgSize.width + svgPadding * 2}
                                 height={svgSize.height + svgPadding * 2} className={"wordcloudsvg"}>
                            </svg>
                        </TransformComponent>
                    </TransformWrapper>
                </Box>

            </Box>
        </Box>
    );
}