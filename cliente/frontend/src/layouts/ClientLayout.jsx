import { Outlet } from 'react-router-dom'
import NavBar from '../components/NavBar.jsx'
import Footer from '../components/Footer.jsx'
import ChatWidget from '../components/ChatWidget.jsx'
import '../pages/Home.css'

export default function ClientLayout(){
  return (
    <>
      <NavBar />
      <Outlet />
      <ChatWidget />
      <Footer />
    </>
  )
}
