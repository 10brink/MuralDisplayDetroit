package co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set; 

import javax.servlet.http.HttpSession;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit.entity.Favorite;
import co.grandcircus.BaddamBoseTenbrick.MuralDisplayDetroit.entity.FavoriteRepository;
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
	
	@Autowired
	FavoriteRepository fr; 

	@RequestMapping("/")
	public ModelAndView homeTest(HttpSession session) {
		session.setAttribute("counter", 0);
		if (((Integer) session.getAttribute("counter")) == 0) {
			session.setAttribute("loggedin", false);
		}
		return new ModelAndView("Index");
	}
	

	@RequestMapping("/art_near_me")
	public ModelAndView displayArt(HttpSession session) {
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
	
	@RequestMapping("userpage")
	public ModelAndView returnToUser(HttpSession session) {
		int i = ((Integer) session.getAttribute("counter")); 
		i++; 
		session.setAttribute("counter", i);
		return new ModelAndView("userpage", "user", session.getAttribute("user")); 
	}
	
	@RequestMapping("/create")
	public ModelAndView createUser() {
		return new ModelAndView("adduser"); 
	}
	@RequestMapping("/display_all_art")
	public ModelAndView displayAllArt(HttpSession session) {
		ModelAndView mv = new ModelAndView("displayallart", "list", mr.findAll());
		if (((Boolean) session.getAttribute("loggedin")) == true) {
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
		return new ModelAndView("Index");
	}
	
	@RequestMapping("addtofavorites")
	public ModelAndView displayUserFavorites(@RequestParam("favorites[]") String favorites, @RequestParam("favoritez") Integer userid) {
		String[] favorites2 = favorites.split(",");
		if (userid != null) {
			for (int i = 0; i < favorites2.length; i++) {
				try {
					fr.save(new Favorite(Integer.parseInt(favorites2[i]), userid));
				} catch (DataIntegrityViolationException e) {
					
				}
			}
			List<Favorite> faves = fr.findByUserid(userid);
			// converts favorites to murals. 
			ArrayList<Mural> faves2 = new ArrayList<Mural>(); 
			for (int i = 0; i < faves.size(); i++) {
				Optional <Mural> m = mr.findById(faves.get(i).getMuralid());
				m.ifPresent(mural -> faves2.add(m.get()));
			}
			
			return new ModelAndView("faves", "faves", faves2);
		} else {
			for (int i = 0; i < favorites2.length; i++) {
				fr.save(new Favorite(Integer.parseInt(favorites2[i])));
			}
			return new ModelAndView("redirect:/");
		} 
		
	}
	
	@RequestMapping("recommendations")
	public ModelAndView recommendations(HttpSession session) {
		User user = ((User)session.getAttribute("user"));
		Integer userid = user.getUserid();  
		List<Favorite> userFavorites = fr.findByUserid(userid);
		//It maps the user_id with list of favorites 
		HashMap<Integer, List<Favorite>> favoriteLists = new HashMap<Integer, List<Favorite>>(); 
		List<Favorite> everything = fr.findAll(); 

		//List of userid's for reference 
		ArrayList<Integer> userids = new ArrayList<Integer>();
		//List of users that have a lot of common favorites
		ArrayList<Integer> commonFavoriteUsers = new ArrayList<Integer>(); 
		//Add user id's to the list without adding the non user associated favorites
		for (int i = 0; i < everything.size(); i++) {
			if (!(userids.contains(everything.get(i).getUserid())) && everything.get(i).getUserid()!= null) {
				userids.add(everything.get(i).getUserid()); 
			}
		}
		//fills the favorite_list hash map
		for (int i = 0; i < userids.size(); i++) {
			favoriteLists.put(userids.get(i), fr.findByUserid(userids.get(i)));
		}
		
		/* for (int i = 0; i < favoriteLists.size(); i++) {
			System.out.println(favoriteLists.get(userids.get(i)));
		} */
		
		 
		for (int i = 0; i < favoriteLists.size(); i++) {
			int commonCount = 0;
			int smallerListSize = 0;
			//The following if else statement determines whether the current list in 
			//the hash map or the users list is smaller
			if (userFavorites.size() < favoriteLists.get(userids.get(i)).size()) {
				smallerListSize = userFavorites.size();
			} else {
				smallerListSize = favoriteLists.get(userids.get(i)).size();
			}
			//counts the number of common favorites
			for (int j = 0; j < favoriteLists.get(userids.get(i)).size(); j++) {
				for (int k = 0; k < userFavorites.size(); k++) {
					if (favoriteLists.get(userids.get(i)).get(j).getMuralid() == userFavorites.get(k).getMuralid()) {
						commonCount++; 
					}
				}
			}
			
			if (commonCount >= smallerListSize / 4) {
				commonFavoriteUsers.add(userids.get(i));
			}
			 
			/*for (int l = 0; l < commonFavoriteUsers.size(); l++) {
			System.out.println(commonFavoriteUsers.get(l));
			 } */
		}
		//Converts the  user favorites to the mural ids
		ArrayList<Integer> favoriteMuralIds = new ArrayList<Integer>(); 
		for (int i = 0; i < userFavorites.size(); i++) {
			favoriteMuralIds.add(userFavorites.get(i).getMuralid());
		}
		
		ArrayList<Integer> recommendedExtraMurals = new ArrayList<Integer>(); 
		//Every user for whom had a significant number of common favorites with the current user, is then converted into a list of favorites
		for (int i = 0; i < commonFavoriteUsers.size(); i++) {
			List<Favorite> entryFavorites = fr.findByUserid(commonFavoriteUsers.get(i));
			for (int j = 0; j < entryFavorites.size(); j++) {
				//for each of the favorites in that user's list, if the current user does NOT have that as a favorite, it is added to a recommended favorites list
				if (!(favoriteMuralIds.contains(entryFavorites.get(j).getMuralid()))) {
					recommendedExtraMurals.add(entryFavorites.get(j).getMuralid());
				}
			}
		}
		
		ArrayList<Mural> recs = new ArrayList<Mural>(); 
		//the mural ids in the recommended favorite's list is converted to a list of murals
		for (int i = 0; i < recommendedExtraMurals.size(); i++)  {
			System.out.println(recommendedExtraMurals.get(i));
			recs.add(mr.getOne(recommendedExtraMurals.get(i)));
		}
		
		
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
