function outval = normalized(img, num)
outval(:, :, 1)=(img(:, :, 1) - min(min(img(:, :, 1)))) / ...
            (max(max(img(:, :, 1))) - min(min(img(:, :, 1)))) * num;
outval(:, :, 2)=(img(:, :, 2) - min(min(img(:, :, 2)))) / ...
            (max(max(img(:, :, 2))) - min(min(img(:, :, 2)))) * num;
outval(:, :, 3)=(img(:, :, 3) - min(min(img(:, :, 3)))) / ...
            (max(max(img(:, :, 3))) - min(min(img(:, :, 3)))) * num;