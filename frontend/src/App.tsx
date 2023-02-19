import React from 'react';
import TagDrawer from "./TagDrawer";
import {createTheme, ThemeProvider} from "@mui/material";

function App() {

    const theme = createTheme({
        breakpoints: {
            values: {
                xs: 0,
                sm: 800,
                md: 900,
                lg: 1200,
                xl: 1536,
            },
        },
        palette: {
            primary: {
                main: '#4ebeff',
            },
            secondary: {
                main: '#E12885',
            },
        },
        components: {
            MuiCssBaseline: {
                styleOverrides: `
            ::-webkit-scrollbar{
                width: 0.35em;
            },
            ::-webkit-scrollbar-thumb {
                background-color: #4ebeff80;
                border-radius: 0.2em;
            }
            `
            },
        },
    })

    return (
        <ThemeProvider theme={theme}>
            <TagDrawer window={() => window}/>
        </ThemeProvider>
    );
}

export default App;
