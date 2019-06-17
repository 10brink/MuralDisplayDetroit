package co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit.entity.Mural;
import co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit.entity.User;
import co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit.entity.MuralRepository;
import co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit.entity.UserRepository;

@Controller
public class MuralController {
	
	@Value("${map.key}")
	String mapkey;
	
	@Autowired
	MuralRepository mr;
	
	@Autowired
	UserRepository ur; 

	@RequestMapping("/")
	public ModelAndView homeTest(HttpSession session) {
		session.setAttribute("loggedin", false);
		return new ModelAndView("Index");
	}
	

	@RequestMapping("/art_near_me")
	public ModelAndView displayArt() {
		List<Mural> murals = mr.findAll(); 
		ModelAndView mv = new ModelAndView("artnearme", "murals", murals); 
		mv.addObject("mapkey", mapkey);
		
		return mv;
		
	}
	
	@RequestMapping("/login") 
	public ModelAndView loginPage() {
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
				return new ModelAndView("error"); 
			}
		} else {
			return new ModelAndView("erroruser"); 
		}
	}
	
	@RequestMapping("/create")
	public ModelAndView createUser() {
		return new ModelAndView("adduser"); 
	}
	@RequestMapping("/display_all_art")
	public ModelAndView displayAllArt(HttpSession session) {
		ModelAndView mv = new ModelAndView("displayallart", "list", mr.findAll());
		if (((Boolean) session.getAttribute("loggedin")) == true) {
			System.out.println("Nick is chewing on his sweater");
			mv.addObject("userid", ((User) session.getAttribute("user")).getUserid()); 
		}
		return mv; 
	}
	
	
	@RequestMapping("/confirmation")
	public ModelAndView confirmation(@RequestParam("username") String username, @RequestParam("password") String password) {
		ur.save(new User(username, password)); 
		return new ModelAndView("confirmationpage"); 
	}
	
	@RequestMapping("faves")
	public ModelAndView favoriteMuralsPerUser(@RequestParam("user") Integer id) {
		try {
			String favorites = ur.getOne(id).getMuralids();
			String[] muralidstring = favorites.split(",");
			Integer[] muralids = new Integer[muralidstring.length];
			for (int i = 0; i < muralidstring.length; i++) {
				muralids[i] = Integer.parseInt(muralidstring[i]);
			}
			ArrayList<Mural> murals = new ArrayList<Mural>(); 
			for (int i = 0; i < muralids.length; i++) {
				Optional<Mural> m = mr.findById(muralids[i]); 
				if (m.isPresent()) {
					Mural m2 = m.get();
					murals.add(m2); 
				} 
			} 
			return new ModelAndView("favorites", "murals", murals);
		} catch (NullPointerException e) {
			System.out.println("Sorry empty favorites");
			return null; 
		}
		
	}	
	
	@RequestMapping("addtofavorites")
	public ModelAndView displayUserFavorites(@RequestParam("favorites[]") String[] favorites, @RequestParam("favoritez") Integer userid) {
		
		String muralids = ""; 
		for (int i = 0; i < favorites.length; i++) {
			muralids += favorites[i]; 
			if (i != favorites.length - 1) {
				muralids += ",";
			}
		}
		System.out.println(muralids + "flying pizza man");
		ur.updateFavorites(muralids, userid);
		for (int i = 0; i < favorites.length; i++) {
			List<Mural> e = mr.findAll(); 
			Mural m = new Mural(); 
			for (int i1 = 0; i1 < e.size(); i1++) {
				if (e.get(i1).getMuralid() == userid) {
					m = e.get(i1); 
				}
			}
			int favoriteCount = m.getFavoritecount(); 
			favoriteCount++; 
			mr.updateFavoriteCount(m.getMuralid(), favoriteCount);
		}
		return new ModelAndView("redirect:/faves");
	}
	
}
