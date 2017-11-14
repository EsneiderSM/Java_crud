/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import DAO.DAOCompra;
import DAO.DAOTarjetaXCliente;
import DAO.DAOTipoTarjeta;
import Model.ModelCompra;
import Model.ModelInfoProyeccion;
import Model.ModelListProyeccion;
import Model.ModelProyeccion;
import Model.ModelTarjetaXCliente;
import Model.ModelTipoTarjeta;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author esneiderserna
 */
public class ControllerCompra extends HttpServlet {
    
    private final DAOCompra DAO = new DAOCompra();
    private final String VIEW_LISTA = "Compra/Lista.jsp";
    private final String VIEW_PROYECCION = "Compra/Proyeccion.jsp";
    private final String VIEW_CREAR = "Compra/Crear.jsp";
    private ModelCompra shopping;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        String option = (String) request.getParameter("opcion");
        
        if(option == null)
        {
            if(request.getParameter("create") != null)
            {
                CreateShopping(request, response);
            }
            
        }
        else
        {
            if(option.equals("listarCompras"))
            {
                ListShopping(request, response);
            }
            
            if(option.equals("proyeccion"))
            {
                Proyection(request, response);
            }
        }
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void ListShopping(HttpServletRequest request, HttpServletResponse response) 
    {
        
        try {
            
            List<ModelCompra> listShoppings = DAO.ListShopping();
            request.setAttribute("listShoppings", listShoppings);
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
    }

    private void Proyection(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        
        try {
        
            ModelCompra compra = DAO.DetailShopping(Integer.parseInt(request.getParameter("idCompra")));
            RequestDispatcher vista;
            
            
            ModelListProyeccion proyec = GenerateProyection(compra);
            
            request.setAttribute("proyec", proyec);
            vista = request.getRequestDispatcher(VIEW_PROYECCION);
            vista.forward(request, response);
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
    
    }

    private ModelListProyeccion GenerateProyection(ModelCompra compra) 
    {
        
        ModelListProyeccion proyection = new ModelListProyeccion();
        List<ModelInfoProyeccion> infoProyec = new LinkedList<>();
        
        int cuotas = compra.getNumeroCuotas_Compra();
        int valorCompra = compra.getDeudaInicial_Compra();
        double interes = compra.getInteres_Compra();
        int cuotaMensual = valorCompra/cuotas;
        int vSaldo = compra.getDeudaInicial_Compra();
        
        proyection.setInfoCompra(compra);
        proyection.setCuota_Mensual(cuotaMensual);
        
        for (int i = 1; i <= cuotas; i++) {
            ModelInfoProyeccion m = new ModelInfoProyeccion();
            double vInteres = vSaldo * interes;
            int nSaldo = vSaldo - cuotaMensual;
            double vCuota = cuotaMensual + vInteres;
            
            m.setNumero_Couta(i);
            m.setFecha(compra.getFecha_Compra());
            m.setValor_Saldo(vSaldo);
            m.setAbono_Capital(cuotaMensual);
            m.setValor_Interes(vInteres);
            m.setNuevo_Saldo(nSaldo);
            m.setValor_Cuota(vCuota);
            
            vSaldo = nSaldo;
            infoProyec.add(m);
        }
        
        proyection.setInfoProyeccion(infoProyec);
   
        return proyection;
    }

    private void CreateShopping(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        
        try {
            
            boolean error = false;
            DAOTarjetaXCliente daoTarjetaXC = new DAOTarjetaXCliente();
            DAOTipoTarjeta daoTipoTarjeta = new DAOTipoTarjeta();
            
            ModelCompra model = new ModelCompra();
                        model.setId_Compra(0);
                        model.setDescripcion_Compra(request.getParameter("Descripcion"));
                        model.setNumero_TarjetaXCliente(request.getParameter("NoTarjeta"));
                        model.setDeudaInicial_Compra(Integer.parseInt(request.getParameter("VCompra")));
                        model.setDeudaActual_Compra(Integer.parseInt(request.getParameter("VCompra")));
                        model.setNumeroCuotas_Compra(Integer.parseInt(request.getParameter("NCuotas")));
            
                        
            request.setAttribute("idCliente", request.getParameter("IdClient")); 
            request.setAttribute("modelCompra", model); 

            //Consulta la tarjeta del cliente
            ModelTarjetaXCliente mTarjetaXC = daoTarjetaXC.GetInfoCard(model.getNumero_TarjetaXCliente());

            //Valida que exista una tarjata asignada al cliente con el numero
            if(mTarjetaXC != null){
                
                //Consulta el tipo de tarjeta
                ModelTipoTarjeta mTipoTarjeta = daoTipoTarjeta.GetInfoTypeCard(mTarjetaXC.getCodigo_TipoTarjeta());
                // asigno el interes
                model.setInteres_Compra(mTipoTarjeta.getInteres_TipoTarjeta());
                
                //Valida que la tarjeta si pertenezca al cliente
                if(mTarjetaXC.getId_Usuario().equals(request.getParameter("IdClient"))){
                    
                    //Valida que el valor de la compra no sea superior al disponible
                    if(mTarjetaXC.getCupoDisp_TarjetaXCliente() >= model.getDeudaInicial_Compra() && model.getDeudaInicial_Compra() > 0 )
                    {
                        
                        if(model.getNumeroCuotas_Compra() <= mTipoTarjeta.getPlazoMax_TipoTarjeta() && model.getNumeroCuotas_Compra() > 0)
                        {
                            //Guarda la compra
                            int idShopping = DAO.CreateShopping(model);

                            //Actualiza el cupo disponible de la tarjeta
                            mTarjetaXC.setCupoDisp_TarjetaXCliente(mTarjetaXC.getCupoDisp_TarjetaXCliente() - model.getDeudaInicial_Compra());
                            daoTarjetaXC.UpdateInfoCard(mTarjetaXC);

                            //Muestra la proyeccion
                            response.sendRedirect("Compra?opcion=proyeccion&idCompra=" + idShopping);
                        
                        }else{
                            request.setAttribute("errorMessage", "El numero de cuotas debe ser mayor a 0 e inferior o igual a " + mTipoTarjeta.getPlazoMax_TipoTarjeta());
                            error = true;
                        } 
                        
                    }else{
                        request.setAttribute("errorMessage", "Cupo insuficiente.");
                        error = true;
                    }  
                
                }else{
                    request.setAttribute("errorMessage", "La tarjeta no pertece al cliente.");
                    error = true;
                }
            
            }else{
                request.setAttribute("errorMessage", "El número de la tarjeta no es valido.");
                error = true;
            }
            
            if(error){
                request.getRequestDispatcher(VIEW_CREAR).forward(request, response);
            } 
            
        } catch (SQLException e) {
        }
        
    }

}
