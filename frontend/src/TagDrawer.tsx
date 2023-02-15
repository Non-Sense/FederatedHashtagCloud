import * as React from 'react';
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
import WordCloud from "react-d3-cloud";
import {useEffect, useRef} from "react";
import {GetTag} from "./api/GetTag";
import {useGetElementProperty} from "./useGetElementProperty";
import useAppBarHeight from "./useAppBarHeight";

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

    const targetRef = useRef(null);
    const {getElementProperty} = useGetElementProperty<HTMLDivElement>(targetRef);

    const appBarHeight = useAppBarHeight()

    const handleDrawerToggle = () => {
        setMobileOpen(!mobileOpen);
    };

    useEffect(() => {
        GetTag("http://192.168.20.16:8080", data => {
            setTags(data)
        })
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
                ref={targetRef}
                component="main"
                sx={{flexGrow: 1, p: 3, width: {sm: `calc(100% - ${drawerWidth}px)`}, height: {xs: `calc(100vh - ${appBarHeight}px)`}}}
            >
                <Toolbar/>
                <WordCloud
                    data={tags.map(data => ({text: data.name, value: data.count}))}
                    fontSize={d => Math.pow(d.value, 0.6) * 15 + 4}
                    // rotate={d => (Math.random() > 0.5) ? 0 : 90}
                    rotate={0}
                    width={getElementProperty("width")}
                    height={getElementProperty("height")}
                    spiral={"rectangular"}
                    font={"sans-serif"}
                />
            </Box>
        </Box>
    );
}