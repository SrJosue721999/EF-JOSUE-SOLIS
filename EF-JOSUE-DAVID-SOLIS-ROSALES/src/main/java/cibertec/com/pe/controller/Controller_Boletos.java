package cibertec.com.pe.controller;

import cibertec.com.pe.model.Boleto;
import cibertec.com.pe.model.Ciudad;
import cibertec.com.pe.model.Venta;
import cibertec.com.pe.model.Venta_Detalle;
import cibertec.com.pe.repository.Ciudad_Repository;
import cibertec.com.pe.repository.VentaDetalle_Repository;
import cibertec.com.pe.repository.Venta_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
@SessionAttributes({"boletosAgregados"})
public class Controller_Boletos {

    @Autowired
    private Ciudad_Repository ciudadRepository;

    @Autowired
    private Venta_Repository ventaRepository;

    @Autowired
    private VentaDetalle_Repository ventaDetalleRepository;

    @GetMapping("/index")
    public String inicioSlash(Model model) {
        List<Ciudad> ciudades = ciudadRepository.findAll();
        List<Boleto> boletos = (List<Boleto>) model.getAttribute("boletosAgregados");

        if(boletos.size()>0){
            Boleto boletoEncontrado = boletos.get(boletos.size()-1);
            model.addAttribute("boleto", boletoEncontrado);
        }else{
            model.addAttribute("boleto", new Boleto());
        }

        model.addAttribute("ciudades", ciudades);


        return "index";
    }

    @GetMapping("/volver-compra")
    public String volverCompra(Model model) {
        List<Ciudad> ciudades = ciudadRepository.findAll();

        model.addAttribute("boleto", new Boleto());
        model.addAttribute("ciudades", ciudades);
        model.addAttribute("boletosAgregados", new ArrayList<>());

        return "index";
    }

    @GetMapping("/inicio")
    public String inicio(Model model) {
        List<Ciudad> ciudades = ciudadRepository.findAll();
        List<Boleto> boletos = (List<Boleto>) model.getAttribute("boletosAgregados");

        if(boletos.size()>0){
            Boleto boletoEncontrado = boletos.get(boletos.size()-1);
            model.addAttribute("boleto", boletoEncontrado);
        }else{
            model.addAttribute("boleto", new Boleto());
        }

        model.addAttribute("ciudades", ciudades);

        return "index";
    }

    @PostMapping("/agregar-boleto")
    public String agregarBoleto(Model model, @ModelAttribute Boleto boleto) {
        List<Ciudad> ciudades = ciudadRepository.findAll();
        List<Boleto> boletos = (List<Boleto>) model.getAttribute("boletosAgregados");
        Double precioBoleto = 50.00;

        boleto.setSubTotal(boleto.getCantidad() * precioBoleto);

        boletos.add(boleto);

        model.addAttribute("boletosAgregados", boletos);
        model.addAttribute("ciudades", ciudades);
        model.addAttribute("boleto", new Boleto());

        return "redirect:/index";
    }

    @GetMapping("/comprar")
    public String comprar(Model model) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
        List<Boleto> boletos = (List<Boleto>) model.getAttribute("boletosAgregados");
        Double montoTotal = 0.0;

        for (Boleto boleto : boletos) {
            montoTotal += boleto.getSubTotal();
        }

        Venta nuevaVenta = new Venta();
        nuevaVenta.setFechaVenta(new Date());
        nuevaVenta.setMontoTotal(montoTotal);
        nuevaVenta.setNombreComprador(boletos.get(0).getNombreComprador());

        Venta ventaGuardada = ventaRepository.save(nuevaVenta);

        for (Boleto boleto : boletos) {
            Venta_Detalle venta_Detalle = new Venta_Detalle();

            Ciudad ciudadDestino = ciudadRepository.findById(boleto.getCiudadDestino()).get();
            venta_Detalle.setCiudadDestino(ciudadDestino);
            Ciudad ciudadOrigen = ciudadRepository.findById(boleto.getCiudadOrigen()).get();
            venta_Detalle.setCiudadOrigen(ciudadOrigen);

            venta_Detalle.setCantidad(boleto.getCantidad());
            venta_Detalle.setSubTotal(boleto.getSubTotal());

            Date fechaRetorno = formatter.parse(boleto.getFechaRetorno());
            venta_Detalle.setFechaRetorno(fechaRetorno);

            Date fechaSalida = formatter.parse(boleto.getFechaSalida());
            venta_Detalle.setFechaViaje(fechaSalida);

            venta_Detalle.setVenta(ventaGuardada);

            ventaDetalleRepository.save(venta_Detalle);
        }

        return "index";
    }

    @GetMapping("/limpiar")
    public String limpiar(Model model){
        List<Ciudad> ciudades = ciudadRepository.findAll();

        model.addAttribute("boleto", new Boleto());
        model.addAttribute("ciudades", ciudades);

        return "index";
    }

    @ModelAttribute("boletosAgregados")
    public List<Boleto> boletosComprados() {
        return new ArrayList<>();
    }
}
