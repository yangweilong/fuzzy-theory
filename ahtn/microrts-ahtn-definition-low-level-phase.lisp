(defdomain microrts-low-level
	(

		;; ---- ---- ---- ---- ---- 
		;; ---- OPERATORS	
		;; ---- ---- ---- ---- ---- 
        (:operator (!wait ?time ?essential)
			(true)
		)
		
		(:operator (!wait-for-free-unit ?player ?essential)
			(true)
		)
		(:operator (!fill-with-idles ?player ?essential)
			(true)
		)
		(:operator (!idle ?unitid ?essential)
			(unit ?unitid ?_ ?_ ?_ ?_)
		)
		(:operator (!move-into-attack-range ?unitid1 ?unitid2 ?essential)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 ?type2 ?player2 ?r2 ?oldposition2)
				(can-move ?type1)
			)	
		)
		(:operator (!move-into-harvest-range ?unitid1 ?unitid2 ?essential)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 ?type2 ?player2 ?r2 ?oldposition2)
				(can-move ?type1)
			)	
		)
		(:operator (!move-into-return-range ?unitid1 ?unitid2 ?essential)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 ?type2 ?player2 ?r2 ?oldposition2)
				(can-move ?type1)
			)	
		)
		(:operator (!harvest ?unitid1 ?unitid2 ?essential)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 Resource ?_ ?r2 ?oldposition2)
				(can-harvest ?type1)
				(in-harvest-range ?unitid1 ?unitid2)
			)	
		)
		(:operator (!return ?unitid1 ?unitid2 ?essential)
			(and
				(unit ?unitid1 ?type1 ?player1 1 ?oldposition1)
				(unit ?unitid2 Base ?_ ?r2 ?oldposition2)
				(can-harvest ?type1)
				(in-return-range ?unitid1 ?unitid2)
			)	
		)
		(:operator (!attack ?unitid1 ?unitid2 ?essential)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(unit ?unitid2 ?type2 ?player2 ?r2 ?oldposition2)
				(can-attack ?type1)
				(in-attack-range ?unitid1 ?unitid2)
			)	
		)
		(:operator (!produce ?unitid1 ?direction ?type ?essential)
			(and
				(unit ?unitid1 ?type1 ?player1 ?r1 ?oldposition1)
				(can-produce ?type1 ?type)
				(has-resources-to-produce ?player1 ?type)
				(free-building-position (neighbor-position ?oldposition1 ?direction))
			)	
		)


		;; ---- ---- ---- ---- ---- 
		;; ---- METHODS	
		;; ---- ---- ---- ---- ---- 
		;; fully destroy the enemy: one method for each possible action that can be executed (to allow for the full search tree)
		(:method dp-1 (destroy-player ?player1 ?player2 ?essential)
		     (:sequence
			 	(:!condition (and
								(unit ?_ ?_ ?player2 ?_ ?_)
								(unit ?_ ?_ ?player1 ?_ ?_)
							))
			(:phase (phase-dp-1-phase ?player1 ?player2))
			)
		)
	
		(:method dp-win (destroy-player ?player1 ?player2 ?essential)
		(:!condition (and
								(not (unit ?_ ?_ ?player2 ?_ ?_))
								(unit ?_ ?_ ?player1 ?_ ?_)
							))
		)
		(:method dp-lose (destroy-player ?player1 ?player2 ?essential)
				(:!condition (and
								(not (unit ?_ ?_ ?player1 ?_ ?_))
								(unit ?_ ?_ ?player2 ?_ ?_)
							))
		)
		(:method dpi-nextunit (destroy-player-internal ?player1 ?player2 ?lastunit ?essential)
		     (:sequence
		     (:condition (next-available-unit ?lastunit ?player1 ?unit))
				(:phase (phase-dpi-nextunit-phase ?player1 ?player2 ?unit))
			)
		)
		
		(:method dpi-nomoreunits (destroy-player-internal ?player1 ?player2 ?lastunit ?essential)
			(:sequence
				(:condition (no-more-available-units ?lastunit ?player1))
				(:phase (phase-fill-with ?player1))
				(:phase (phase-wait ?player1))
				(:phase (phase-destroy-player ?player1 ?player2))
			)
		)
		
		(:method uo-produce (unit-order ?player1 ?player2 ?unitid1 ?essential)
			(:sequence
				(:condition (and 
								(unit ?unitid1 ?type1 ?player1 ?_ ?_)
								(can-produce ?type1 ?type)
								(has-resources-to-produce ?player1 ?type)
								(free-producing-direction ?unitid1 ?direction)
							))
				(:phase (phase-uo-produce ?unitid1 ?direction ?type))
			)
		)

		(:method uo-attack (unit-order ?player1 ?player2 ?unitid1 ?essential)
		    (:sequence
			 (:condition (and
			  				(unit ?unitid1 ?type1 ?player1 ?_ ?_)
			  				(unit ?unitid2 ?type2 ?player2 ?_ ?_)
							(can-attack ?type1)
			  				))
			  (:phase (phase-uo-attack-phase-1 ?unitid1 ?unitid2))
			  (:condition (in-attack-range ?unitid1 ?unitid2))
			  (:phase (phase-uo-attack-phase-2 ?unitid1 ?unitid2))
			 )
		)
		
		(:method uo-idle (unit-order ?player1 ?player2 ?unitid1 ?essential)
			(:sequence
				(:condition (and 
								(unit ?unitid1 ?type1 ?player1 ?_ ?_)
							))
				(:phase (phase-idle ?unitid1))
			)
		)
		
		(:method uo-harvest (unit-order ?player1 ?player2 ?unitid ?essential)
			(:sequence 
				(:condition (and 
								(unit ?unitid Worker ?player1 ?_ ?_)
								(unit ?resourceid Resource ?_ ?_ ?_)
								(unit ?baseid Base ?player1 ?_ ?_)
							))
				(:phase (phase-uo-harvest-1 ?unitid ?resourceid ?baseid))
				(:phase (phase-uo-harvest-2 ?unitid ?resourceid ?baseid))
				(:phase (phase-uo-harvest-3 ?unitid ?resourceid ?baseid))
				(:phase (phase-uo-harvest-4 ?unitid ?resourceid ?baseid))
			)
		)

		;;------phase-------
		(:phase phase-dp-1-phase (dp-1 ?player1 ?player2)		  
			(:method (destroy-player-internal ?player1 ?player2 -1 1))
		)
		
		(:phase phase-dpi-nextunit-phase (dpi-nextunit ?player1 ?player2 ?unit)
                (:parallel
				  (:method (unit-order ?player1 ?player2 ?unit 1))  
                  (:method (destroy-player-internal ?player1 ?player2 ?unit 1))				  
				)
		)
		
	    (:phase phase-destroy-player (dpi-nomoreunits ?player1 ?player2)
		       (:method (destroy-player ?player1 ?player2 1))
		)
		
		(:phase phase-uo-attack-phase-1 (uo-attack ?unitid1 ?unitid2)
                (:operator (!move-into-attack-range ?unitid1 ?unitid2 1))
		)
		(:phase phase-uo-attack-phase-2 (uo-attack ?unitid1 ?unitid2)
		  	    (:operator (!attack ?unitid1 ?unitid2 1))
		)
		
		(:phase phase-uo-produce (uo-produce ?unitid1 ?direction ?type)
		       (:operator (!produce ?unitid1 ?direction ?type 1))  
		)
		
		(:phase phase-wait (dpi-nomoreunits ?player1)
               (:operator (!wait-for-free-unit ?player1 1)) 
		)
		
		(:phase phase-fill-with (dpi-nomoreunits ?player1)
               (:operator (!fill-with-idles ?player1 1))
		)
		

		(:phase phase-idle (uo-idle ?unitid1)
               (:operator (!idle ?unitid1 1))
		)
		
        
		(:phase phase-uo-harvest-1 (uo-harvest ?unitid ?resourceid ?baseid)
                (:operator (!move-into-harvest-range ?unitid ?resourceid 1))
        )	
        (:phase phase-uo-harvest-2 (uo-harvest ?unitid ?resourceid ?baseid)
				(:operator (!harvest ?unitid ?resourceid 1))
        )	
         (:phase phase-uo-harvest-3 (uo-harvest ?unitid ?resourceid ?baseid)
				(:operator (!move-into-return-range ?unitid ?baseid 1))
        )	
       (:phase phase-uo-harvest-4 (uo-harvest ?unitid ?resourceid ?baseid)
				(:operator (!return ?unitid ?baseid 1))	
        )			
	)
)
