import { StrictMode } from 'react'
import React from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  //StrictMode is a tool for highlighting potential problems in an application.
  //  It activates additional checks and warnings for its descendants.
  //  StrictMode does not render any visible UI.
  //  It only activates additional checks and warnings for its descendants.
  //  StrictMode is a development tool and does not affect the production build.
  //<React.StrictMode> is same as <StrictMode> and 
  // the difference is that <React.StrictMode> is the default
  // export of the 'react' package and <StrictMode>
  //  is a named export of the 'react' package.
  // We can use either <React.StrictMode> or <StrictMode> in our application.
  // depending upon what we import from 'react' package.
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
