from PIL import Image, ImageDraw
import os

# Define icon sizes for different densities
sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

# Create directories and generate icons
for density, size in sizes.items():
    dir_path = f'app/src/main/res/{density}'
    os.makedirs(dir_path, exist_ok=True)
    
    # Generate regular launcher icon (blue square)
    img_regular = Image.new('RGBA', (size, size), (33, 150, 243, 255))
    img_regular.save(f'{dir_path}/ic_launcher.png')
    
    # Generate round launcher icon (blue circle)
    img_round = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img_round)
    draw.ellipse([2, 2, size-2, size-2], fill=(33, 150, 243, 255))
    img_round.save(f'{dir_path}/ic_launcher_round.png')
    
    print(f'Generated {density}: {size}x{size}')

print('Done!')
