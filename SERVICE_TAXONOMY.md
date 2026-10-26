# HireMe — Service Taxonomy & Database Specification

This document defines the official category and sub-category hierarchy, database identifiers, base prices, and architecture for the HireMe application.

---

## 1. Categories & Subcategories Hierarchy

| # | Category | Database ID (`CategoryIds`) | Subcategories |
|---|---|---|---|
| **1** | **Home Care** | `home_care` | • Deep Cleaning<br>• Kitchen Cleaning<br>• Bathroom Cleaning<br>• Cook Service<br>• Utensil Cleaning |
| **2** | **Appliances** | `appliances` | • AC Service & Repair<br>• Washing Machine Repair<br>• Refrigerator Repair |
| **3** | **Electric** | `electric` | • Wiring & Cabling<br>• Inverter and UPS<br>• Switchboard Repair<br>• Fan & Light Repair |
| **4** | **Automotive** | `automotive` | • Driver<br>• Complete Car Wash<br>• Two Wheeler Service<br>• Tyre & Puncture Service<br>• Battery Jumpstart & Check |
| **5** | **Beauty** | `beauty` | • Haircut & Styling<br>• Facial & Cleanup<br>• Manicure & Pedicure<br>• Bridal Makeup |
| **6** | **Plumbing** | `plumbing` | • Leak Repair<br>• Pipe Installation<br>• Drain Cleaning<br>• Toilet Repair |
| **7** | **Health Care** | `health_care` | • Physiotherapy Session<br>• Home Nursing Care<br>• General Physician Consultation<br>• Blood Test |
| **8** | **Furniture** | `furniture` | • Furniture Repair<br>• Furniture Cleaning<br>• Furniture Refurbishing<br>• Furniture Installation |
| **9** | **Installation** | `installation` | • Drilling & AC Installation<br>• RO Installation<br>• Fan & Light Installation |

---

## 2. Detailed Service Catalog & Base Pricing

### 1. Home Care (`home_care`)
- `hc_1` — **Deep Cleaning**: Full home deep-clean for rooms, floors, windows & balcony (₹1,499)
- `hc_2` — **Kitchen Cleaning**: Degreasing cabinets, chimney, gas stove, sink & wall tiles (₹799)
- `hc_3` — **Bathroom Cleaning**: Tile stain removal, commode scrubbing, fixtures & sanitization (₹499)
- `hc_4` — **Cook Service**: Professional home cook for breakfast, lunch, dinner & events (₹899)
- `hc_5` — **Utensil Cleaning**: Complete dishwashing, scrubbing, sink wash & kitchen hygiene (₹349)

### 2. Appliances (`appliances`)
- `app_1` — **AC Service & Repair**: Jet pump service, gas leak check & cooling problem fix (₹699)
- `app_2` — **Washing Machine Repair**: Drum issues, drainage blockage, motor fix & spin cycle repair (₹499)
- `app_3` — **Refrigerator Repair**: Cooling malfunction, thermostat, gas refill & coil cleaning (₹599)

### 3. Electric (`electric`)
- `elec_1` — **Wiring & Cabling**: Internal wiring repair, extension boards & safety check (₹599)
- `elec_2` — **Inverter and UPS**: Inverter battery check, wiring setup & backup troubleshooting (₹499)
- `elec_3` — **Switchboard Repair**: Replacement of burnt switches, sockets, MCB & fuse (₹299)
- `elec_4` — **Fan & Light Repair**: Ceiling fan capacitor, regulator fix & decorative lights (₹349)

### 4. Automotive (`automotive`)
- `auto_1` — **Driver**: Experienced professional on-demand driver for city or highway (₹499)
- `auto_2` — **Complete Car Wash**: High-pressure foam exterior wash, interior vacuum & dashboard shine (₹699)
- `auto_3` — **Two Wheeler Service**: Full bike tune-up, engine oil change, brake & chain lubrication (₹449)
- `auto_4` — **Tyre & Puncture Service**: On-spot tubeless tyre puncture repair & pressure inspection (₹249)
- `auto_5` — **Battery Jumpstart & Check**: Emergency vehicle battery jumpstart & charging diagnosis (₹399)

### 5. Beauty (`beauty`)
- `bty_1` — **Haircut & Styling**: Professional styling, hair trimming, blow-dry & treatment at home (₹499)
- `bty_2` — **Facial & Cleanup**: Deep pore skin cleanup, anti-tan facial & relaxing massage (₹699)
- `bty_3` — **Manicure & Pedicure**: Nail filing, exfoliating scrub, cuticle care & hand-foot massage (₹599)
- `bty_4` — **Bridal Makeup**: Complete bridal makeup, hairstyling, draping & accessory placement (₹4,999)

### 6. Plumbing (`plumbing`)
- `plumb_1` — **Leak Repair**: Fixing leaking faucets, shower valves, overhead tanks & joints (₹399)
- `plumb_2` — **Pipe Installation**: New water line connections, pipeline replacement & drainage pipes (₹699)
- `plumb_3` — **Drain Cleaning**: Clearing clogged sinks, bathroom floor drains & main sewer traps (₹499)
- `plumb_4` — **Toilet Repair**: Flush valve repair, cistern tank leak & jet spray replacement (₹349)

### 7. Health Care (`health_care`)
- `hlth_1` — **Physiotherapy Session**: Certified therapist for joint mobility, back pain & stroke rehab (₹899)
- `hlth_2` — **Home Nursing Care**: Trained nurse for post-op dressing, injections & vital signs (₹1,199)
- `hlth_3` — **General Physician Consultation**: Doctor home consultation, diagnostic review & medical prescription (₹799)
- `hlth_4` — **Blood Test**: Home sample collection for full body profile & CBC lab testing (₹499)

### 8. Furniture (`furniture`)
- `furn_1` — **Furniture Repair**: Fixing loose dining tables, broken chair joints & cabinet hinges (₹499)
- `furn_2` — **Furniture Cleaning**: Deep shampooing for fabric sofas, dining chairs & mattresses (₹699)
- `furn_3` — **Furniture Refurbishing**: Wood sanding, polish, lacquer finish & upholstery re-covering (₹1,499)
- `furn_4` — **Furniture Installation**: Assembly of new flat-pack beds, wardrobes, study desks & shelves (₹599)

### 9. Installation (`installation`)
- `inst_1` — **Drilling & AC Installation**: Heavy wall drilling, split/window AC indoor-outdoor unit mounting (₹1,299)
- `inst_2` — **RO Installation**: Water purifier mounting, inlet-outlet plumbing & TDS testing (₹499)
- `inst_3` — **Fan & Light Installation**: Ceiling fan installation, chandelier, tube lights & wall lamps (₹349)

---

## 3. Dual-Login Model & Role-Based Routing

The platform supports two distinct user roles:
1. **Customer (`UserRole.CUSTOMER`)**:
   - Logs in / Registers with "Customer" selected.
   - Accesses `Routes.CUSTOMER_HOME` (`HomeScreen`).
   - Browses 9 categories, selects sub-services, submits requests, tracks active requests, rates workers, and completes payment to HireMe.
2. **Worker (`UserRole.WORKER`)**:
   - Logs in / Registers with "Worker" selected.
   - Accesses `Routes.WORKER_HOME` (`WorkerHomeScreen`).
   - Toggles "Accept Requests", views incoming customer jobs, accepts jobs, and marks them complete when finished at customer's house.

---

## 4. Future Database Schema Mapping

When transitioning to Firebase Firestore, Room SQLite, or a REST API:
- **`categories` collection/table**:
  - `id: String` (Primary Key, matching `CategoryIds`)
  - `title: String`
  - `description: String`
  - `iconKey: String`
- **`sub_services` collection/table**:
  - `id: String` (Primary Key)
  - `categoryId: String` (Foreign Key -> `categories.id`)
  - `name: String`
  - `description: String`
  - `basePrice: Int`
- **`requests` collection/table**:
  - `id: String`
  - `customerId: String`
  - `workerId: String?`
  - `subServiceId: String`
  - `status: String` (`PENDING`, `ACCEPTED`, `IN_PROGRESS`, `COMPLETED`, `RATED`, `PAID`)
  - `address: String`
  - `description: String`
  - `createdAt: Long`
