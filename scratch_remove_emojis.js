const fs = require('fs');
const path = require('path');

// Regex to match emojis
// Using a broad emoji regex
const emojiRegex = /([\u2700-\u27BF]|[\uE000-\uF8FF]|\uD83C[\uDC00-\uDFFF]|\uD83D[\uDC00-\uDFFF]|[\u2011-\u26FF]|\uD83E[\uDD10-\uDDFF])/g;

const targetExts = ['.js', '.jsx', '.html', '.css', '.java', '.md', '.txt', '.yml', '.properties'];
const excludeDirs = ['node_modules', 'dist', 'target', '.git', '.m2', 'apache-maven-3.9.6'];

function removeEmojisFromFile(filePath) {
    try {
        let content = fs.readFileSync(filePath, 'utf8');
        if (emojiRegex.test(content)) {
            const newContent = content.replace(emojiRegex, '');
            fs.writeFileSync(filePath, newContent, 'utf8');
            console.log(`Removed emojis from: ${filePath}`);
        }
    } catch (e) {
        console.error(`Error processing ${filePath}: ${e.message}`);
    }
}

function scanDir(dir) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const fullPath = path.join(dir, file);
        const stat = fs.statSync(fullPath);
        if (stat.isDirectory()) {
            if (!excludeDirs.includes(file)) {
                scanDir(fullPath);
            }
        } else {
            const ext = path.extname(file);
            if (targetExts.includes(ext)) {
                removeEmojisFromFile(fullPath);
            }
        }
    }
}

const rootDir = process.argv[2] || '.';
console.log(`Scanning ${rootDir} for emojis...`);
scanDir(rootDir);
console.log('Done.');
