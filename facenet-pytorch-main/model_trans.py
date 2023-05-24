#%%
import torch
from torch.utils.mobile_optimizer import optimize_for_mobile
from facenet import Facenet
#%%
model = Facenet()
model = model.generate()
model.eval()  # 将模型设为验证模式
example = torch.rand(1, 3, 160, 160)  # 输入样例的格式为一张224*224的3通道图像
traced_script_module = torch.jit.trace(model, example)
traced_script_module_optimized = optimize_for_mobile(traced_script_module)
traced_script_module_optimized._save_for_lite_interpreter("model.pt")

