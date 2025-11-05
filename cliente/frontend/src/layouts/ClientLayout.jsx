import { Outlet } from 'react-router-dom'
import NavBar from '../components/NavBar.jsx'
import '../pages/Home.css'

export default function ClientLayout(){
  return (
    <>
      <NavBar />
      <Outlet />
    </>
  )
}
