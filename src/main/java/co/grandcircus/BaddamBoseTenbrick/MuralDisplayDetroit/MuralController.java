package co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.TreeSet;
import javax.servlet.ServletContext;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit.entity.Favorite;
import co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit.entity.FavoriteRepository;
import co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit.entity.Mural;
import co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit.entity.MuralRepository;
import co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit.entity.User;
import co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit.entity.UserRepository;

@Controller
public class MuralController {
	
	@Value("${map.key}")
	String mapkey;
	
	@Autowired
	ServletContext context; 
	
	@Autowired
	MuralRepository mr;
	
	@Autowired
	UserRepository ur; 
	
	@Autowired
	FavoriteRepository fr; 

	HelperMethods hm = new HelperMethods(); 
	
	@RequestMapping("/")
	public ModelAndView homeTest(HttpSession session) {
		if (((Integer) session.getAttribute("counter")) == null) {
			session.setAttribute("loggedin", false);
			session.setAttribute("counter", -15);
		}
		
		ModelAndView mv = new ModelAndView("Index");
		
		int num = mr.findAll().size();
		Random rand = new Random();
		int rand1 = rand.nextInt(num);
		Mural mural = mr.findById(rand1).orElse(null);

		mv.addObject("mural", mural);
		return mv;
	}
	

	@RequestMapping("/art_near_me")
	public ModelAndView displayArt(HttpSession session) {
		List<Mural> murals = mr.findAll(); 
		ModelAndView mv = new ModelAndView("artnearme", "murals", murals); 
		mv.addObject("mapkey", mapkey);
		if (((Boolean) session.getAttribute("loggedin")) == true) {
			mv.addObject("userid", ((User) session.getAttribute("user")).getUserid()); 
		}
		return mv;
		
	}
	
	@RequestMapping("/login") 
	public ModelAndView loginPage(HttpSession session) {
		if(((Boolean)session.getAttribute("loggedin"))== true)
		{
			return new ModelAndView("userpage","user",session.getAttribute("user"));
			
		}
		return new ModelAndView("login");
	}
	
	@RequestMapping("/loggingin")
	public ModelAndView login(HttpSession session, @RequestParam("username") String username, @RequestParam("password") String password) {
		User user = ur.findByUsername(username); 
		if (user != null) {
			if (user.getPassword().equals(password)) {
				session.setAttribute("loggedin", true);
				session.setAttribute("user", user);
				ModelAndView mv = new ModelAndView("userpage", "user", session.getAttribute("user")); 
				mv.addObject("session", session); 
				return mv; 
			} else {
				return new ModelAndView("errorpass"); 
			}
		} else {
			return new ModelAndView("erroruser"); 
		}
	}
	
	@RequestMapping("userpage")
	public ModelAndView returnToUser(HttpSession session) {
		return new ModelAndView("userpage", "user", session.getAttribute("user")); 
	}
	
	@RequestMapping("/create")
	public ModelAndView createUser() {
		return new ModelAndView("adduser"); 
	}
	
	@RequestMapping("/neighborhood")
	public ModelAndView displayNeighborhood(HttpSession session) {
		List<Mural> murals = mr.findAll(); 
		List<Mural> mural =  mr.findAllByOrderByNeighborhoodAsc();
			
		
		HashMap<String, List<Mural>> neighborhood = new HashMap<String, List<Mural>>(); 
		List<String> nlist = mr.findDistinctNeighborhood();
		
		for(int i=0;i< nlist.size();i++) {
			neighborhood.put(nlist.get(i), mr.findAllByNeighborhood(nlist.get(i)));	
			//neighborhood.put(mural.get(i), murals.get(i));
			//System.out.println(Arrays.asList(neighborhood));
		}
		
		ModelAndView mv = new ModelAndView("neighborhood","list", neighborhood);
		if  (((Boolean) session.getAttribute("loggedin")) == true) {
			mv.addObject("userid", ((User)session.getAttribute("user")).getUserid()); 
		}
		
		//mv.addObject("neighbors", mr.findDistinctMurals());
		return mv;
	}
	@RequestMapping("/display_all_art")
	public ModelAndView displayAllArt(HttpSession session) {
		ModelAndView mv = new ModelAndView("displayallart", "list", mr.findAll());
		if (((Boolean) session.getAttribute("loggedin")) == true) {
			mv.addObject("userid", ((User) session.getAttribute("user")).getUserid()); 
		}
		return mv; 
	}
	@RequestMapping("/upload_art")
	public ModelAndView uploadArt() {
		return new ModelAndView("uploadArt"); 
	}
	
	@RequestMapping("/upload")
	public ModelAndView fileUpload(@RequestParam("picture") MultipartFile picture, @RequestParam("url") String url, @RequestParam("name") String name, @RequestParam("artist") String artist, @RequestParam("address") String address, @RequestParam("neighborhood") String neighborhood) {
		
		String uploadPath = context.getRealPath("/") + "WEB-INF" + File.separator + "views" + File.separator + "UserMurals" + File.separator;
		try {
			FileCopyUtils.copy(picture.getBytes(), new File(uploadPath+picture.getOriginalFilename()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Spring is literal garbage"); 
		}
        mr.save(new Mural("" + uploadPath + picture.getOriginalFilename(), 42.335960, -83.049751, address, neighborhood, name, artist));
		return new ModelAndView("uploadconfirmation"); 
		
		
	}
	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();
	 
	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	@RequestMapping("selectionCheckIn")
	public ModelAndView selectionCheckIn(@RequestParam("selection") Integer muralid) {
		Mural m = mr.getOne(muralid);
		return new ModelAndView("finalCheckIn", "mural", m);
		
	}
	@RequestMapping("/checkin")
	public ModelAndView checkin(@RequestParam("lattitude") String lat, @RequestParam("longitude") String lon) {
		List <Mural> murals = mr.findAll(); 
		ArrayList<Double> lattitude = new ArrayList<Double>(); 
		ArrayList<Double> longitude = new ArrayList<Double>(); 
		ArrayList<Mural> m = new ArrayList<Mural>(); 
		for (int i = 0; i < murals.size(); i++) {
			lattitude.add(round(murals.get(i).getLatitude(), 3));
			longitude.add(round(murals.get(i).getLongitude(), 3));
		}
		int index = 0; 
		for (int i = 0; i < lattitude.size(); i++) {
			if (round(Double.parseDouble(lat), 3) == lattitude.get(i)) {
				index = i; 
			}
			if (round(Double.parseDouble(lon), 3) == longitude.get(index)) {
				if(!(m.contains(murals.get(index)))) {
					m.add(murals.get(index)); 
				}
			}
		}
		
		return new ModelAndView("CheckIn", "mural", m);
		
	}
	
	
	@RequestMapping("/confirmation")
	public ModelAndView confirmation(@RequestParam("username") String username, @RequestParam("password") String password) {
		try {
			ur.save(new User(username, password)); 
		}  catch (DataIntegrityViolationException e){
			return new ModelAndView("errorcreate");
		}
		return new ModelAndView("confirmationpage"); 
	}
	
	@RequestMapping("faves")
	public ModelAndView favoriteMuralsPerUser(@RequestParam("user") Integer id) {
		List<Favorite> faves = fr.findByUserid(id);
		ArrayList<Mural> faves2 = new ArrayList<Mural>(); 
		for (int i = 0; i < faves.size(); i++) {
			//Optional is an advanced hibernate Query class which represents the possibility of a mural 
			//being found by the query
			Optional <Mural> m = mr.findById(faves.get(i).getMuralid());
			// this takes a function as an argument and executes only if the mural. 
			m.ifPresent(mural -> faves2.add(m.get()));
		}
		
		return new ModelAndView("faves", "faves", faves2);
	}
	
	@RequestMapping("logout") 
	public ModelAndView logout(HttpSession session) {
		session.setAttribute("loggedin", false);
		session.removeAttribute("user");
		session.setAttribute("counter", null);
		
		int num = mr.findAll().size();
		Random rand = new Random();
		int rand1 = rand.nextInt(num);
		
		ModelAndView mv=new ModelAndView("Index");
		Mural mural = mr.findById(rand1).orElse(null);

		mv.addObject("mural", mural);
		return mv;
		 
	}
	
	@RequestMapping("addtofavorites")
	public ModelAndView displayUserFavorites(@RequestParam("favorites[]") String favorites, @RequestParam("favoritez") Integer userid) {
		String[] favorites2 = favorites.split(",");
			hm.addFavorites(favorites2, userid);
			return new ModelAndView("redirect:/");
		
	}
	
	
	
	@RequestMapping("recommendations")
	public ModelAndView recommendations(HttpSession session) {
		
		ArrayList<Mural> recs = hm.findRecommendations(session);
		
		// finally, the list of murals is sent to the jsp page for display. 
		ModelAndView mv = new ModelAndView("recommendations", "recommendations", recs);
		mv.addObject("user", session.getAttribute("user"));
		return mv; 
		
	}
	
	@RequestMapping("addrecs")
	public ModelAndView addRecs(HttpSession session, @RequestParam("muralid[]") String muralid, @RequestParam("user") Integer userid) {
		String[] murals = muralid.split(",");
		Integer[] muralids = new Integer[murals.length];
		for (int i = 0; i < murals.length; i++) {
			muralids[i] = Integer.parseInt(murals[i]);
		}
		for (int i = 0; i < muralids.length; i++) {
			fr.save(new Favorite(muralids[i], userid));
		}
		return new ModelAndView("redirect:/userpage", "user", session.getAttribute("user"));
	}
}
