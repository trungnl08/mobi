const fs = require('fs');

const db = {
    donations: []
}

fs.writeFile('./db.json', JSON.stringify(db), () => {
    console.log("Succesfully")
})
