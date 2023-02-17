import * as React from 'react';
import {useCallback, useEffect, useRef} from 'react';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import CssBaseline from '@mui/material/CssBaseline';
import Drawer from '@mui/material/Drawer';
import IconButton from '@mui/material/IconButton';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import MenuIcon from '@mui/icons-material/Menu';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import TagData from "./vo/TagData";
import {GetTag} from "./api/GetTag";
import {useGetElementProperty} from "./useGetElementProperty";
import useAppBarHeight from "./useAppBarHeight";
import * as d3 from "d3";
import {D3ZoomEvent, zoom} from "d3";
import {TransformComponent, TransformWrapper} from "react-zoom-pan-pinch";

const drawerWidth = 240;

interface Props {
    /**
     * Injected by the documentation to work in an iframe.
     * You won't need it on your project.
     */
    window?: () => Window;
}

export default function TagDrawer(props: Props) {
    const {window} = props;
    const [mobileOpen, setMobileOpen] = React.useState(false);
    const [tags, setTags] = React.useState<Array<TagData>>([]);

    const svgRef = useRef<SVGSVGElement>(null);

    const appBarHeight = useAppBarHeight()

    const handleDrawerToggle = () => {
        setMobileOpen(!mobileOpen);
    };

    useEffect(() => {
        let ignore = false;
        if (ignore)
            return;

        console.log("fetch")
        GetTag("http://192.168.100.4:8080", data => {
            setTags(data)
        })

        if(svgRef.current != null) {
            const img = svgRef.current.getElementsByTagName("image");
            if(img != null) {
                console.log(img.item(0)?.childElementCount);
            }
        }

        return () => {
            ignore = true;
        }
    }, [])

    const drawer = (
        <div>
            {/*<Toolbar/>*/}
            {/*<Divider/>*/}
            <List>
                {tags.map(data => (
                    <ListItem key={data.name} disablePadding>
                        <ListItemButton>
                            <ListItemText primary={data.name}/>
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
                    <Typography variant="h6" noWrap component="div">
                        Responsive drawer
                    </Typography>
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
                bgcolor={"#0f0"}
            >
                <Toolbar/>
                <Box sx={{width: {sm: `100%`}, height: {xs: `calc(100vh - ${appBarHeight}px)`}, p: 0, m: 0}}
                     bgcolor={"#000"}>
                    <TransformWrapper initialScale={1.2} limitToBounds={true} minScale={0.3} disablePadding={true}
                                      centerOnInit={true}>
                        <TransformComponent wrapperStyle={{height: `calc(100vh - ${appBarHeight}px)`, width: `100%`}}>
                            <svg ref={svgRef} width={1400} height={1400} className={"wcsvg"}>
                                <image href={"/wd.svg"}/>
                            </svg>
                        </TransformComponent>
                    </TransformWrapper>
                </Box>

            </Box>
        </Box>
    );
}